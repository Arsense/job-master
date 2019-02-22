package com.job.learn.man.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tangwei
 * @date 2019/2/22 13:54
 */
public class JobFailMonitorHelper {

    private static Logger logger = LoggerFactory.getLogger(JobFailMonitorHelper.class);

    private static JobFailMonitorHelper instance = new JobFailMonitorHelper();
    public static JobFailMonitorHelper getInstance(){
        return instance;
    }

    // ---------------------- monitor ----------------------

    private Thread monitorThread;
    private volatile boolean toStop = false;

    public void start() {

    }
}
