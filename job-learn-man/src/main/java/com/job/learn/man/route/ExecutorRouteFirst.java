package com.job.learn.man.route;

import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TriggerParam;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/3/18 18:41
 */
public class ExecutorRouteFirst extends ExecutorRouter {
    @Override
    public Result<String> route(TriggerParam triggerParam, List<String> addressList) {
        return new Result<String>(addressList.get(0));
    }
}
