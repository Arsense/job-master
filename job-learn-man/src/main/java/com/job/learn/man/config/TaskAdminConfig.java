package com.job.learn.man.config;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import com.job.learn.man.dao.TaskRegistryMapper;
import com.learn.job.core.executor.domain.TaskRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/** 调度器配置 管理注入各种Bean
 *
 * @author tangwei
 * @date 2019/2/18 19:42
 */
@Configuration
public class TaskAdminConfig implements InitializingBean {
    private static TaskAdminConfig adminConfig = null;
    /**
     *  国际化读取配置
     */
    @Value("${task.i18n}")
    private String i18n;
    //job相关配置信息
    @Resource
    private TaskInfoMapper taskInfoMapper;
    //job相关集群信息
    @Resource
    private TaskGroupMapper taskGroupMapper;
    //job日志相关
    @Resource
    private TaskLogMapper taskLogMapper;
    @Resource
    private TaskRegistryMapper taskRegistryMapper;

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public TaskInfoMapper getTaskInfoMapper() {
        return taskInfoMapper;
    }

    public TaskGroupMapper getTaskGroupMapper() {
        return taskGroupMapper;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public static TaskAdminConfig getAdminConfig() {
        return adminConfig;
    }

    public TaskLogMapper getTaskLogMapper() {
        return taskLogMapper;
    }

    public TaskRegistryMapper getTaskRegistryMapper() {
        return taskRegistryMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
    }
}
