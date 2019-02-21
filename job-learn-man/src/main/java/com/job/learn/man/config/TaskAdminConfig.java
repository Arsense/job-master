package com.job.learn.man.config;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/** 一个管理端配置吧
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

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
    }
}
