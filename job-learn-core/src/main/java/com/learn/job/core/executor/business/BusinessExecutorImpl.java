package com.learn.job.core.executor.business;

import com.learn.job.core.executor.AbstractJobExecutor;
import com.learn.job.core.executor.AbstractJobHandler;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TriggerParam;
import com.learn.job.core.executor.enums.GlueTypeEnum;
import com.learn.job.core.executor.glue.GlueFactory;
import com.learn.job.core.executor.handler.GlueJobHandler;
import com.learn.job.core.executor.thread.TaskThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tangwei
 * @date 2019/2/20 10:40
 */
public class BusinessExecutorImpl implements BusinessExecutor {

    private static Logger logger = LoggerFactory.getLogger(BusinessExecutorImpl.class);


    /**
     * 执行任务的线程去执行相应的任务 JobThread->去运行JobHandler
     * @param triggerParam
     * @return
     */
    @Override
    public Result<String> run(TriggerParam triggerParam) {
        // load old：jobHandler + jobThread
        TaskThread jobThread = AbstractJobExecutor.loadJobThread(triggerParam.getJobId());
        AbstractJobHandler jobHandler = jobThread!=null ? jobThread.getHandler() : null;
        String removeOldReason = null;
        //GLUE 语言的选择执行不同的命令和脚本吧
        GlueTypeEnum glueTypeEnum = GlueTypeEnum.match(triggerParam.getGlueType());
        //通过注解@jobHandler实现的
        if (GlueTypeEnum.BEAN == glueTypeEnum) {
            //Spring加载时就扫描加载进去了 直接get获取即可
            AbstractJobHandler newJobHandler = AbstractJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());
            // valid old jobThread
            if (jobThread!=null && jobHandler != newJobHandler) {
                // change handler, need kill old thread
                removeOldReason = "change jobhandler or glue type, and terminate the old job thread.";
                jobThread = null;
                jobHandler = null;
                // valid handler
                if (jobHandler == null) {
                    try {
                        AbstractJobHandler originJobHandler = GlueFactory.getInstance().loadNewInstance(triggerParam.getGlueSource());
                        jobHandler = new GlueJobHandler(originJobHandler, triggerParam.getGlueUpdatetime());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        return new Result<String>(Result.FAIL_CODE, e.getMessage());
                    }
                }

            } else {
                return new Result<String>(Result.FAIL_CODE, "glueType[" + triggerParam.getGlueType() + "] is not valid.");
            }

            if (jobThread == null) {
                jobThread = AbstractJobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
            }

            // push data to queue
            Result<String> pushResult = jobThread.pushTriggerQueue(triggerParam);
            return pushResult;
        }
        return Result.SUCCESS;
    }
}
