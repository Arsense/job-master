package com.job.learn.man.monitor;

import com.job.learn.man.config.TaskAdminConfig;
import com.job.learn.man.thread.TaskTriggerPoolHelper;
import com.job.learn.man.trigger.TriggerTypeEnum;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TaskGroup;
import com.learn.job.core.executor.domain.TaskInfo;
import com.learn.job.core.executor.domain.TaskLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                                // lock log 锁定日志
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
                                if (info != null
                                        && info.getAlarmEmail() != null
                                        && info.getAlarmEmail().trim().length() > 0) {
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

    /**
     * 确认报警 发送邮件
     * @param info
     * @param jobLog
     * @return
     */
    private boolean failAlarm(TaskInfo info, TaskLog jobLog) {
        boolean alarmResult = true;
        if (info == null || info.getAlarmEmail() == null || info.getAlarmEmail().trim().length() <= 0) {
            return alarmResult;
        }
        try {
            // 报警信息里面加入 是否触发成功 或者是句柄是否成的信息
            String alarmContent = "Alarm Job LogId=" + jobLog.getId();
            if (jobLog.getTriggerCode() != Result.SUCCESS_CODE) {
                alarmContent += "<br>TriggerMsg=<br>" + jobLog.getTriggerMsg();
            }
            if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != Result.SUCCESS_CODE) {
                alarmContent += "<br>HandleCode=" + jobLog.getHandleMsg();
            }


            //因为人员可能是多个 所以需要遍历发送

            Set<String> emailSet = new HashSet<String>(Arrays.asList(info.getAlarmEmail().split(",")));

            for (String email: emailSet) {
                TaskGroup group = TaskAdminConfig
                        .getAdminConfig()
                        .getTaskGroupMapper()
                        .loadById(info.getJobGroup());

                String personal = I18nUtil.getString("admin_name_full");
                String title = I18nUtil.getString("jobconf_monitor");
                String content = MessageFormat.format(mailBodyTemplate,
                        group!=null?group.getTitle():"null",
                        info.getId(),
                        info.getJobDesc(),
                        alarmContent);

                //构造发送邮件
                MimeMessage mimeMessage = TaskAdminConfig.getAdminConfig().getMailSender().createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(TaskAdminConfig.getAdminConfig().getEmailUserName(), personal);
                helper.setTo(email);
                helper.setSubject(title);
                helper.setText(content, true);
                TaskAdminConfig.getAdminConfig().getMailSender().send(mimeMessage);
            }
        } catch (Exception e) {
                logger.error(">>>>>>>>>>> 分布式Job, job fail alarm email send error, JobLogId:{}", jobLog.getId(), e);

                alarmResult = false;
        }
        // TODO, custom alarm strategy, such as sms
        return alarmResult;
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



    // ---------------------- alarm ----------------------

    // 报警邮件前端基础的模板
    private static final String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</span>" +
            "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
            "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
            "      <tr>\n" +
            "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobgroup") +"</td>\n" +
            "         <td width=\"10%\" >"+ I18nUtil.getString("jobinfo_field_id") +"</td>\n" +
            "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobdesc") +"</td>\n" +
            "         <td width=\"10%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_title") +"</td>\n" +
            "         <td width=\"40%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_content") +"</td>\n" +
            "      </tr>\n" +
            "   </thead>\n" +
            "   <tbody>\n" +
            "      <tr>\n" +
            "         <td>{0}</td>\n" +
            "         <td>{1}</td>\n" +
            "         <td>{2}</td>\n" +
            "         <td>"+ I18nUtil.getString("jobconf_monitor_alarm_type") +"</td>\n" +
            "         <td>{3}</td>\n" +
            "      </tr>\n" +
            "   </tbody>\n" +
            "</table>";
}
