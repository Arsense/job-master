package com.job.learn.man.monitor;

import com.job.learn.man.config.TaskAdminConfig;
import com.job.learn.man.thread.TaskTriggerPoolHelper;
import com.job.learn.man.trigger.TriggerTypeEnum;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.domain.TaskInfo;
import com.learn.job.core.executor.domain.TaskLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tangwei
 * @date 2019/2/22 13:54
 */
public class JobFailMonitorHelper {

    private static Logger logger = LoggerFactory.getLogger(JobFailMonitorHelper.class);

    private static JobFailMonitorHelper instance = new JobFailMonitorHelper();
    public static JobFailMonitorHelper getInstance(){
        return instance;
    }

    // ---------------------- monitor ----------------------
    private Thread monitorThread;
    private volatile boolean toStop = false;

    public void start() {
        monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // monitor
                while (!toStop) {
                    try {
                        //查询表中状态错误的日志1000条 根据ID扫描
                        List<Integer> failLogIds = TaskAdminConfig.getAdminConfig().getTaskLogMapper().findFailJobLogIds(1000);
                        if (failLogIds != null && !failLogIds.isEmpty()) {
                            for (int failLogId : failLogIds) {
                                // lock log
                                int lockRet = TaskAdminConfig.getAdminConfig().getTaskLogMapper().updateAlarmStatus(failLogId, 0, -1);
                                if (lockRet < 1) {
                                    continue;
                                }
                                TaskLog log = TaskAdminConfig.getAdminConfig().getTaskLogMapper().loadById(failLogId);
                                TaskInfo info = TaskAdminConfig.getAdminConfig().getTaskInfoMapper().loadById(log.getId());

                                // 1、fail retry monitor
                                if (log.getExecutorFailRetryCount() > 0) {
                                    TaskTriggerPoolHelper.trigger(log.getId(), TriggerTypeEnum.RETRY, (log.getExecutorFailRetryCount() - 1), log.getExecutorShardingParam(), null);
                                    String retryMsg = "<br><br><span style=\"color:#F39C12;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_type_retry") + "<<<<<<<<<<< </span><br>";
                                    log.setTriggerMsg(log.getTriggerMsg() + retryMsg);
                                    TaskAdminConfig.getAdminConfig().getTaskLogMapper().updateTriggerInfo(log);
                                }

                                // 2、fail alarm monitor
                                int newAlarmStatus = 0;        // 告警状态：0-默认、-1=锁定状态、1-无需告警、2-告警成功、3-告警失败
                                if (info != null && info.getAlarmEmail() != null && info.getAlarmEmail().trim().length() > 0) {
                                    boolean alarmResult = true;
                                    try {
                                        alarmResult = failAlarm(info, log);
                                    } catch (Exception e) {
                                        alarmResult = false;
                                        logger.error(e.getMessage(), e);
                                    }
                                    newAlarmStatus = alarmResult ? 2 : 3;
                                } else {
                                    newAlarmStatus = 1;
                                }

                                TaskAdminConfig.getAdminConfig().getTaskLogMapper().updateAlarmStatus(failLogId, -1, newAlarmStatus);
                            }
                        }

                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(">>>>>>>>>>> xxl-job, job fail monitor thread error:{}", e);
                        }
                    }
                }
            }
        });
        //设置为后台进程
        monitorThread.setDaemon(true);
        monitorThread.start();

    }

    private boolean failAlarm(TaskInfo info, TaskLog log) {
        return false;
    }


    public void toStop(){
        toStop = true;
        // interrupt and wait
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
