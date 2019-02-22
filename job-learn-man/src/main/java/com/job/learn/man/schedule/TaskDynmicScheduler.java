package com.job.learn.man.schedule;

import com.job.learn.man.config.TaskAdminConfig;
import com.job.learn.man.monitor.JobFailMonitorHelper;
import com.job.learn.man.monitor.JobRegistryMonitorHelper;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.business.BusinessExecutor;
import com.learn.job.core.executor.route.ExecutorBlockStrategyEnum;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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

    // scheduler
    private static Scheduler scheduler;
    public void setScheduler(Scheduler scheduler) {
        TaskDynmicScheduler.scheduler = scheduler;
    }

    /**
     * Bean加载前调用
     */
    private void start() {
//        // valid 检验调度器是否为空
//        Assert.notNull(scheduler, "quartz scheduler is null");
//
//        // init i18n
////        initI18n();
//
//        // 运行注册监控
//        JobRegistryMonitorHelper.getInstance().start();
//
//        // 运行情况监控
//        JobFailMonitorHelper.getInstance().start();
//
//        // 初始化RPCserver
//        initRpcProvider();

        logger.info(">>>>>>>>> init job 调度器管理读 启动成功admin success.");

    }


    /**
     * 初始化RPC服务端
     */
    private void initRpcProvider() {
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




}
