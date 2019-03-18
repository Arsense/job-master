package com.job.learn.man.schedule;

import com.job.learn.man.config.TaskAdminConfig;

import com.job.learn.man.jobbean.RemoteHttpJobBean;
import com.job.learn.man.monitor.JobFailMonitorHelper;
import com.job.learn.man.monitor.JobRegistryMonitorHelper;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.business.AdminBusiness;
import com.learn.job.core.executor.business.BusinessExecutor;
import com.learn.job.core.executor.domain.TaskInfo;
import com.job.learn.man.route.ExecutorBlockStrategyEnum;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.net.impl.jetty.server.JettyServerHandler;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.serialize.Serializer;
import org.eclipse.jetty.server.Request;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础的quartz scheduler调度器
 * @author tangwei
 * @date 2019/2/20 10:10
 */
public class TaskDynmicScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TaskDynmicScheduler.class);
    //存储基本的调度执行器
    private static ConcurrentHashMap<String, BusinessExecutor> businessExecutorMap = new ConcurrentHashMap<String, BusinessExecutor>();
    private static JettyServerHandler jettyServerHandler;
    // scheduler
    private static Scheduler scheduler;
    public void setScheduler(Scheduler scheduler) {
        TaskDynmicScheduler.scheduler = scheduler;
    }

    /**
     * Bean加载前调用
     */
    private void start() {
        // valid 检验调度器是否为空
        Assert.notNull(scheduler, "quartz scheduler is null");
        // init i18n
        initI18n();

        // 运行注册监控
        JobRegistryMonitorHelper.getInstance().start();

        // 运行情况监控
        JobFailMonitorHelper.getInstance().start();
//
        // 初始化RPCserver
        initRpcProvider();

        logger.info(">>>>>>>>> init job 调度器管理读 启动成功 admin success.");

    }


    /**
     * 初始化RPC服务端
     */
    private void initRpcProvider() {
        // 创建服务提供者  给core端用AdminBusinesService 是因为那个让其能操作数据库
        XxlRpcProviderFactory xxlRpcProviderFactory = new XxlRpcProviderFactory();
        xxlRpcProviderFactory.initConfig(
                NetEnum.JETTY,
                Serializer.SerializeEnum.HESSIAN.getSerializer(),
                null,
                0,
                TaskAdminConfig.getAdminConfig().getAccessToken(),
                null,
                null);

        // add services  1参名字 二参具体的接口
        xxlRpcProviderFactory.addService(AdminBusiness.class.getName(), null, TaskAdminConfig.getAdminConfig().getAdminBusiness());

        // jetty handler
        jettyServerHandler = new JettyServerHandler(xxlRpcProviderFactory);

    }

    public static void invokeAdminService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        jettyServerHandler.handle(null, new Request(null, null), request, response);
    }


    private void initI18n(){
        for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
    }

    /**
     * Bean周期结束时调用
     */
    private void destroy() {
    }


    public static BusinessExecutor getBusinessExecutor(String address) throws Exception {
        // valid
        if (address==null || address.trim().length()==0) {
            return null;
        }
        //加载缓存
        address = address.trim();
        BusinessExecutor businessExecutor = businessExecutorMap.get(address);
        if (businessExecutor != null) {
            return businessExecutor;
        }

        // set-cache
        businessExecutor = (BusinessExecutor) new XxlRpcReferenceBean(
                NetEnum.JETTY,
                Serializer.SerializeEnum.HESSIAN.getSerializer(),
                CallType.SYNC,
                LoadBalance.ROUND,
                BusinessExecutor.class,
                null,
                10000,
                address,
                TaskAdminConfig.getAdminConfig().getAccessToken(),
                null,
                null).getObject();

        businessExecutorMap.put(address, businessExecutor);
        return businessExecutor;

    }

    /**
     * 移除相应的JOB
     * @param jobName
     * @param jobGroup
     */
    public static boolean removeJob(String jobName, String jobGroup) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);

        if (scheduler.checkExists(triggerKey)) {
            scheduler.unscheduleJob(triggerKey);    // trigger + job
        }

        logger.info(">>>>>>>>>>> removeJob success, triggerKey:{}", triggerKey);
        return true;

    }

    /**
     * 添加相应的JOB到任务队列中
     * add trigger + job
     * @param jobName
     * @param jobGroup
     * @param cronExpression
     * @return
     */
    public static boolean addJob(String jobName, String jobGroup, String cronExpression) throws SchedulerException {
// 1、job key
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName, jobGroup);
        // 2、valid
        if (scheduler.checkExists(triggerKey)) {
            return true;    // PASS
        }

        // 3、corn trigger
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();   // withMisfireHandlingInstructionDoNothing 忽略掉调度终止过程中忽略的调度
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        // 4、job detail
        Class<? extends Job> jobClass_ = RemoteHttpJobBean.class;   // Class.forName(jobInfo.getJobClass());
        JobDetail jobDetail = JobBuilder.newJob(jobClass_).withIdentity(jobKey).build();
        Date date = scheduler.scheduleJob(jobDetail, cronTrigger);

        logger.info(">>>>>>>>>>> addJob success, jobDetail:{}, cronTrigger:{}, date:{}", jobDetail, cronTrigger, date);
        return true;

    }

    public static void fillJobInfo(TaskInfo jobInfo) {

        String group = String.valueOf(jobInfo.getJobGroup());
        String name = String.valueOf(jobInfo.getId());

        // trigger key
        TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
        try {

            // trigger cron
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger!=null && trigger instanceof CronTriggerImpl) {
                String cronExpression = ((CronTriggerImpl) trigger).getCronExpression();
                jobInfo.setJobCron(cronExpression);
            }

            // trigger state
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            if (triggerState!=null) {
                jobInfo.setJobStatus(triggerState.name());
            }

            //JobKey jobKey = new JobKey(jobInfo.getJobName(), String.valueOf(jobInfo.getJobGroup()));
            //JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            //String jobClass = jobDetail.getJobClass().getName();

        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
