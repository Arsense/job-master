package com.learn.job.core.executor.business;

import com.learn.job.core.executor.domain.LogResult;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TriggerParam;

/**
 * @author tangwei
 * @date 2019/2/20 10:13
 */
public interface BusinessExecutor {
    /**
     * run
     * @param triggerParam
     * @return
     */
    public Result<String> run(TriggerParam triggerParam);

    /**
     * log
     * @param logDateTime
     * @param logId
     * @param fromLineNume
     * @return
     */
    public Result<LogResult> log(long logDateTime, int logId, int fromLineNume);
}
