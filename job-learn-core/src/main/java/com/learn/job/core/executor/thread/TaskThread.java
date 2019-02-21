package com.learn.job.core.executor.thread;

import com.learn.job.core.executor.AbstractJobExecutor;
import com.learn.job.core.executor.AbstractJobHandler;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TriggerParam;
import com.learn.job.core.executor.log.JobFileAppender;
import com.learn.job.core.executor.log.JobLogger;
import com.learn.job.core.executor.util.ShardingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author tangwei
 * @date 2019/2/20 16:52
 */
public class TaskThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(TaskThread.class);
    private int jobId;
    private AbstractJobHandler handler;

    private LinkedBlockingQueue<TriggerParam> triggerQueue;
    private Set<Integer> triggerLogIdSet;
    //任务运行的状态
    private boolean running = false;    // if running job
    //任务调度的次数
    private int runTimes = 0;			// idel times


    private volatile boolean toStop = false;
    private String stopReason;

    public TaskThread(int jobId, AbstractJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingQueue<TriggerParam>();
        this.triggerLogIdSet = Collections.synchronizedSet(new HashSet<Integer>());
    }


    /**
     * job任务执行
     */
    @Override
    public void run() {
        // init
        try {
            handler.init();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        while(!toStop){
            running = false;
            runTimes++;

            TriggerParam triggerParam = null;
            Result<String> result = null;
            //检查停止信号，我们需要循环，所以我不能使用queue.take（），而不是poll（timeout）
            try {
                triggerParam = triggerQueue.poll(3L, TimeUnit.SECONDS);
                if (triggerParam != null) {
                    running = true;
                    runTimes = 0;
                    triggerLogIdSet.remove(triggerParam.getLogId());
                    // log filename, like "logPath/yyyy-MM-dd/9999.log"
                    String logFileName = JobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTim()), triggerParam.getLogId());
                    JobFileAppender.contextHolder.set(logFileName);
                    ShardingUtil.setShardingVo(new ShardingUtil.ShardingVO(triggerParam.getBroadcastIndex(), triggerParam.getBroadcastTotal()));
                    // execute
                    JobLogger.log("<br>----------- xxl-job job execute start -----------<br>----------- Param:" + triggerParam.getExecutorParams());
                    if (triggerParam.getExecutorTimeout() > 0) {
                        Thread futureThread = null;

                        try {
                            final TriggerParam triggerParamTmp = triggerParam;
                            FutureTask<Result<String>> futureTask = new FutureTask<Result<String>>(new Callable<Result<String>>() {
                                @Override
                                public Result<String> call() throws Exception {
                                    //调用JOB里面的方法了
                                    return handler.execute(triggerParamTmp.getExecutorParams());
                                }
                            });
                            futureThread = new Thread(futureTask);
                            futureThread.start();

                            result = futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            JobLogger.log("<br>----------- xxl-job job execute timeout");
                            JobLogger.log(e);
                            result = new Result<String>(AbstractJobHandler.FAIL_TIMEOUT.getCode(), "job execute timeout ");
                        } finally {
                            futureThread.interrupt();
                        }
                    } else {
                        // just execute
                        result = handler.execute(triggerParam.getExecutorParams());
                    }
                    if (result == null) {
                        result = AbstractJobHandler.FAIL;
                    }
                    JobLogger.log("<br>----------- xxl-job job execute end(finish) -----------<br>----------- ReturnT:" + result);
                    } else {
                        if (runTimes > 30) {
                            AbstractJobExecutor.removeJobThread(jobId, "excutor idel times over limit.");
                        }
                    }
                }
             catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            handler.destroy();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        logger.info(">>>>>>>>>>> xxl-job JobThread stoped, hashCode:{}", Thread.currentThread());
    }


    /**
     * 将触发后的信息放到相应的Set 这里保留ID是为了
     * @param triggerParam
     * @return
     */
    public Result<String> pushTriggerQueue(TriggerParam triggerParam) {
        // avoid repeat
        if (triggerLogIdSet.contains(triggerParam.getLogId())) {
            logger.info(">>>>>>>>>>> repeate trigger job, logId:{}", triggerParam.getLogId());
            return new Result<String>(Result.FAIL_CODE, "repeate trigger job, logId:" + triggerParam.getLogId());
        }
        triggerLogIdSet.add(triggerParam.getLogId());
        triggerQueue.add(triggerParam);
        return Result.SUCCESS;
    }
    /**
     * kill job thread
     *
     * @param stopReason
     */
    public void toStop(String stopReason) {
        /**
         * Thread.interrupt只支持终止线程的阻塞状态(wait、join、sleep)，
         * 在阻塞出抛出InterruptedException异常,但是并不会终止运行的线程本身；
         * 所以需要注意，此处彻底销毁本线程，需要通过共享变量方式；
         */
        this.toStop = true;
        this.stopReason = stopReason;
    }



    public static Logger getLogger() {
        return logger;
    }

    public int getJobId() {
        return jobId;
    }

    public AbstractJobHandler getHandler() {
        return handler;
    }
}
