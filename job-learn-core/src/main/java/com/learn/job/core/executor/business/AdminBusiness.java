package com.learn.job.core.executor.business;

import com.learn.job.core.executor.domain.HandleCallbackParam;
import com.learn.job.core.executor.domain.Result;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/17 16:23
 */
public interface AdminBusiness {

    public static final String MAPPING = "/api";
    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    public Result<String> callback(List<HandleCallbackParam> callbackParamList);
}
