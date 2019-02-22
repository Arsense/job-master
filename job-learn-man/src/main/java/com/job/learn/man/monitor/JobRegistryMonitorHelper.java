package com.job.learn.man.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tangwei
 * @date 2019/2/22 13:54
 */
public class JobRegistryMonitorHelper {


    private static Logger logger = LoggerFactory.getLogger(JobRegistryMonitorHelper.class);

    private static JobRegistryMonitorHelper instance = new JobRegistryMonitorHelper();
    public static JobRegistryMonitorHelper getInstance(){
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;


    public void start(){

    }
}
