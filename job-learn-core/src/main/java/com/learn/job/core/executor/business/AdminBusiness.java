package com.learn.job.core.executor.business;

import com.learn.job.core.executor.domain.HandleCallbackParam;
import com.learn.job.core.executor.domain.RegistryParam;
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


    /**
     * 注册 相关的服务
     *
     * @param registryParam
     * @return
     */
    public Result<String> registry(RegistryParam registryParam);

    /**
     * 移除相关的注册服务
     *
     * @param registryParam
     * @return
     */
    public Result<String> registryRemove(RegistryParam registryParam);
}
