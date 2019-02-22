package com.job.learn.man.jobbean;

import com.job.learn.man.thread.TaskTriggerPoolHelper;
import com.job.learn.man.trigger.TriggerTypeEnum;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author tangwei
 * @date 2019/2/22 14:59
 */
public class RemoteHttpJobBean extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(RemoteHttpJobBean.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // load jobId
        JobKey jobKey = jobExecutionContext.getTrigger().getJobKey();
        Integer jobId = Integer.valueOf(jobKey.getName());

        // trigger
        TaskTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.CRON, -1, null, null);
    }
}
