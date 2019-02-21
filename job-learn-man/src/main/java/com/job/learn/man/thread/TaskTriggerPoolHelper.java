package com.job.learn.man.thread;

import com.job.learn.man.trigger.TaskTrigger;
import com.job.learn.man.trigger.TriggerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tangwei
 * @date 2019/2/18 19:57
 */
public class TaskTriggerPoolHelper {
    private static TaskTriggerPoolHelper triggerHelper = new TaskTriggerPoolHelper();

    private static Logger logger = LoggerFactory.getLogger(TaskTriggerPoolHelper.class);
    private ThreadPoolExecutor triggerPool = new ThreadPoolExecutor(
            32,
            256,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000));
    /**
     * @param jobId
     * @param triggerType
     * @param failRetryCount  失败尝试次数
     * 			>=0: use this param
     * 			<0: use param from job info config
     * @param executorShardingParam
     * @param executorParam
     *          null: use job param
     *          not null: cover job param
     */
    public static void trigger(int jobId, TriggerTypeEnum triggerType, int failRetryCount, String executorShardingParam, String executorParam) {
        triggerHelper.addTrigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam);
    }



    public void addTrigger(final int jobId, final TriggerTypeEnum triggerType, final int failRetryCount, final String executorShardingParam, final String executorParam) {
        triggerPool.execute(new Runnable() {
            @Override
            public void run() {
                TaskTrigger.trigger(jobId, triggerType, failRetryCount, executorShardingParam, executorParam);
            }
        });
    }

}
