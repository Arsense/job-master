package com.job.learn.man.schedule;

import com.job.learn.man.config.TaskAdminConfig;
import com.learn.job.core.executor.business.BusinessExecutor;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 基础的quartz scheduler调度器
 * @author tangwei
 * @date 2019/2/20 10:10
 */
public class TaskDynmicScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TaskDynmicScheduler.class);
    //存储基本的调度执行器
    private static ConcurrentHashMap<String, BusinessExecutor> businessExecutorMap = new ConcurrentHashMap<String, BusinessExecutor>();
    public static BusinessExecutor getBusinessExecutor(String address) throws Exception {
        // valid
        if (address==null || address.trim().length()==0) {
            return null;
        }
        //加载缓存
        address = address.trim();
        BusinessExecutor businessExecutor = businessExecutorMap.get(address);
        if (businessExecutor != null) {
            return businessExecutor;
        }

        // set-cache
        businessExecutor = (BusinessExecutor) new XxlRpcReferenceBean(
                NetEnum.JETTY,
                Serializer.SerializeEnum.HESSIAN.getSerializer(),
                CallType.SYNC,
                LoadBalance.ROUND,
                BusinessExecutor.class,
                null,
                10000,
                address,
                TaskAdminConfig.getAdminConfig().getAccessToken(),
                null,
                null).getObject();

        businessExecutorMap.put(address, businessExecutor);
        return businessExecutor;

    }



}
