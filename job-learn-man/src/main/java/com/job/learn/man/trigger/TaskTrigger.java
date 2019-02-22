package com.job.learn.man.trigger;

import com.job.learn.man.config.TaskAdminConfig;
import com.job.learn.man.schedule.TaskDynmicScheduler;
import com.learn.job.core.executor.business.BusinessExecutor;
import com.learn.job.core.executor.domain.*;
import com.learn.job.core.executor.route.ExecutorBlockStrategyEnum;
import com.job.learn.man.domain.ExecutorRouteStrategyEnum;
import com.job.learn.man.util.I18nUtil;
import com.xxl.rpc.util.IpUtil;
import com.xxl.rpc.util.ThrowableUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author tangwei
 * @date 2019/2/19 11:31
 */
public class TaskTrigger {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTrigger.class);

    /**
     * 触发相应的JOB
     * @param jobId
     * @param triggerType
     * @param failRetryCount
     * @param executorShardingParam
     * @param executorParam
     */
    public static void trigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorShardingParam, String executorParam) {
            TaskInfo taskInfo = TaskAdminConfig.getAdminConfig().getTaskInfoMapper().findById(jobId);
        if (taskInfo == null) {
            LOG.warn("触发失败, jobId 不存在，jobId={}", jobId);
            return;
        }
            if (executorParam != null) {
            taskInfo.setExecutorParam(executorParam);
        }

        failRetryCount = failRetryCount >= 0 ? failRetryCount:taskInfo.getExecutorFailRetryCount();
        TaskGroup group = TaskAdminConfig.getAdminConfig().getTaskGroupMapper().loadById(taskInfo.getJobGroup());

        //第三参校验 共享变量 默认为空
        int[] shardingParam = null;
        if (executorShardingParam != null){
            String[] shardingArr = executorShardingParam.split("/");
            if (shardingArr.length == 2
                    && StringUtils.isNumeric(shardingArr[0])
                    && StringUtils.isNumeric(shardingArr[1])) {
                shardingParam = new int[2];
                shardingParam[0] = Integer.valueOf(shardingArr[0]);
                shardingParam[1] = Integer.valueOf(shardingArr[1]);
            }
        }
        //路由策略是广播？
        if (ExecutorRouteStrategyEnum.SHARDING_BROADCAST == ExecutorRouteStrategyEnum.match(taskInfo.getExecutorRouteStrategy(), null)
                && group.getRegistryList()!=null
                && !group.getRegistryList().isEmpty()
                && shardingParam==null) {
            for (int i = 0; i < group.getRegistryList().size(); i++) {
                processTrigger(group, taskInfo, failRetryCount, triggerType, i, group.getRegistryList().size());
            }
        } else {

        }

    }

    /**
     *
     * @param group
     * @param taskInfo
     * @param failRetryCount
     * @param triggerType
     * @param index
     * @param total
     */
    private static void processTrigger(TaskGroup group, TaskInfo taskInfo, int failRetryCount, TriggerTypeEnum triggerType, int index, int total) {
        //集群策略与路由策略
        ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(taskInfo.getExecutorBlockStrategy(), ExecutorBlockStrategyEnum.SERIAL_EXECUTION);
        ExecutorRouteStrategyEnum executorRouteStrategyEnum = ExecutorRouteStrategyEnum.match(taskInfo.getExecutorRouteStrategy(), null);
        String shardingParam = (ExecutorRouteStrategyEnum.SHARDING_BROADCAST==executorRouteStrategyEnum)?String.valueOf(index).concat("/").concat(String.valueOf(total)) : null;

        // 1、save log-id
        TaskLog jobLog = new TaskLog();
        jobLog.setJobGroup(taskInfo.getJobGroup());
        jobLog.setJobId(taskInfo.getId());
        jobLog.setTriggerTime(new Date());
        TaskAdminConfig.getAdminConfig().getTaskLogMapper().saveLog(jobLog);
        LOG.debug(">>>>>>>>>>> xxl-job trigger start, jobId:{}", jobLog.getId());

        TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(taskInfo.getId());
        triggerParam.setExecutorHandler(taskInfo.getExecutorHandler());
        triggerParam.setExecutorParams(taskInfo.getExecutorParam());
        triggerParam.setExecutorBlockStrategy(taskInfo.getExecutorBlockStrategy());
        triggerParam.setExecutorTimeout(taskInfo.getExecutorTimeout());
        triggerParam.setLogId(jobLog.getId());
        triggerParam.setLogDateTim(jobLog.getTriggerTime().getTime());
        triggerParam.setGlueType(taskInfo.getGlueType());
        triggerParam.setGlueSource(taskInfo.getGlueSource());
        triggerParam.setGlueUpdatetime(taskInfo.getGlueUpdatetime().getTime());
        triggerParam.setBroadcastIndex(index);
        triggerParam.setBroadcastTotal(total);

        String address = null;
        Result<String> routeAddressResult = null;

        if (group.getRegistryList()!=null && !group.getRegistryList().isEmpty()) {
            //广播 没有路由策略
            if (ExecutorRouteStrategyEnum.SHARDING_BROADCAST == executorRouteStrategyEnum) {
                if (index < group.getRegistryList().size()) {
                    address = group.getRegistryList().get(index);
                } else {
                    address = group.getRegistryList().get(0);
                }
            } else {
                routeAddressResult = executorRouteStrategyEnum.getRouter().route(triggerParam, group.getRegistryList());
                if (routeAddressResult.getCode() == Result.SUCCESS_CODE) {
                    address = routeAddressResult.getData();
                }
            }
        }

        // 4、trigger remote executor
        Result<String> triggerResult = null;
        if (address != null) {
            //核心功能函数
            triggerResult = runExecutor(triggerParam, address);
        } else {
            triggerResult = new Result<String>(Result.FAIL_CODE, null);
        }

        //这里功能已完毕 到返回页面的HTML了
        // 5、collection trigger info
        StringBuffer triggerMsgSb = new StringBuffer();
        triggerMsgSb.append(I18nUtil.getString("jobconf_trigger_type")).append("：").append(triggerType.getTitle());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_admin_adress")).append("：").append(IpUtil.getIp());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regtype")).append("：")
                .append( (group.getAddressType() == 0)?I18nUtil.getString("jobgroup_field_addressType_0"):I18nUtil.getString("jobgroup_field_addressType_1") );
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobconf_trigger_exe_regaddress")).append("：").append(group.getRegistryList());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorRouteStrategy")).append("：").append(executorRouteStrategyEnum.getTitle());
        if (shardingParam != null) {
            triggerMsgSb.append("(" + shardingParam + ")");
        }
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorBlockStrategy")).append("：").append(blockStrategy.getTitle());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_timeout")).append("：").append(taskInfo.getExecutorTimeout());
        triggerMsgSb.append("<br>").append(I18nUtil.getString("jobinfo_field_executorFailRetryCount")).append("：").append(failRetryCount);

        triggerMsgSb.append("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" +
                ""+ I18nUtil.getString("jobconf_trigger_run") +"<<<<<<<<<<< </span><br>")
                .append((routeAddressResult != null && routeAddressResult.getMessage() != null)
                        ?routeAddressResult.getMessage()+"<br><br>":"")
                .append(triggerResult.getMessage() != null ? triggerResult.getMessage():"");


    }

    public static Result<String> runExecutor(TriggerParam triggerParam, String address){
        Result<String> runResult = null;
        try {
            BusinessExecutor businessExecutor = TaskDynmicScheduler.getBusinessExecutor(address);
            runResult = businessExecutor.run(triggerParam);
        } catch (Exception e) {
            LOG.error(">>>>>>>>>>> xxl-job trigger error, please check if the executor[{}] is running.", address, e);
            runResult = new Result<String>(Result.FAIL_CODE, ThrowableUtil.toString(e));
        }
        return null;
    }

}
