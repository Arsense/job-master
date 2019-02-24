package com.job.learn.man.monitor;

import com.job.learn.man.config.TaskAdminConfig;
import com.learn.job.core.executor.domain.TaskGroup;
import com.learn.job.core.executor.domain.TaskInfo;
import com.learn.job.core.executor.domain.TaskRegistry;
import com.learn.job.core.executor.enums.RegistryConfig;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    // auto registry group
                    List<TaskGroup> groupList = TaskAdminConfig.getAdminConfig().getTaskGroupMapper().findByAddressType(0);
                    if (groupList != null && !groupList.isEmpty()) {
                        // remove dead address (admin/executor)
                        TaskAdminConfig.getAdminConfig().getTaskRegistryMapper().removeDead(RegistryConfig.DEAD_TIMEOUT);

                        // fresh online address (admin/executor)
                        HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
                        List<TaskRegistry> list = TaskAdminConfig.getAdminConfig().getTaskRegistryMapper().findAll(RegistryConfig.DEAD_TIMEOUT);
                        if (list != null) {
                            for (TaskRegistry item: list) {
                                if (RegistryConfig.RegistType.EXECUTOR.name().equals(item.getRegistryGroup())) {
                                    String appName = item.getRegistryKey();
                                    List<String> registryList = appAddressMap.get(appName);
                                    if (registryList == null) {
                                        registryList = new ArrayList<String>();
                                    }

                                    if (!registryList.contains(item.getRegistryValue())) {
                                        registryList.add(item.getRegistryValue());
                                    }
                                    appAddressMap.put(appName, registryList);
                                }
                            }
                        }
                        // fresh group address
                        for (TaskGroup group: groupList) {
                            List<String> registryList = appAddressMap.get(group.getAppName());
                            String addressListStr = null;
                            if (registryList!=null && !registryList.isEmpty()) {
                                Collections.sort(registryList);
                                addressListStr = StringUtils.join(registryList, ",");
                            }
                            group.setAddressList(addressListStr);
                            TaskAdminConfig.getAdminConfig().getTaskGroupMapper().updateTaskGroup(group);
                        }
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
            }
        });
        //设置后台线程
        registryThread.setDaemon(true);
        registryThread.start();

    }
}
