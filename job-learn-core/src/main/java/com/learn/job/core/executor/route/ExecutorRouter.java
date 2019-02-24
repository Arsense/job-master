package com.learn.job.core.executor.route;

import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/19 15:58
 */
public abstract class ExecutorRouter  {
    protected static Logger logger = LoggerFactory.getLogger(ExecutorRouter.class);

    /**
     * route address
     *
     * @param addressList
     * @return  Result.content=address
     */
    public abstract Result<String> route(TriggerParam triggerParam, List<String> addressList);
}
