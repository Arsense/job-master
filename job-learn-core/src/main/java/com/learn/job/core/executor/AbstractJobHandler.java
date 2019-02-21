package com.learn.job.core.executor;

import com.learn.job.core.executor.domain.Result;

/**
 * @author tangwei
 * @date 2019/2/17 14:17
 */
public abstract class AbstractJobHandler {


    /** success */
    public static final Result<String> SUCCESS = new Result<String>(200, null);
    /** fail */
    public static final Result<String> FAIL = new Result<String>(500, null);
    /** fail timeout */
    public static final Result<String> FAIL_TIMEOUT = new Result<String>(502, null);

    public abstract Result<String> execute(String param) throws Exception;


    /**
     * 留接口作为继承扩展
     * init handler, invoked when JobThread init
     */
    public void init() {
        // TODO
    }


    /**
     * destroy handler, invoked when JobThread destroy
     */
    public void destroy() {
        // TODO
    }


}
