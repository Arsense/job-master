package com.job.learn.man.config;

import com.job.learn.man.schedule.TaskDynmicScheduler;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

/**
 * <pre>
 *     初始化调度器
 * <pre/>
 *
 * @author tangwei
 * @date 2019/2/22 11:07
 */
@Configuration
public class TaskDynamicSchedulerConfig {

    /**
     * 初始化底层quatz调度器
     * @param dataSource
     * @return
     */
    @Bean
    public SchedulerFactoryBean getSchedulerFactoryBean(DataSource dataSource){
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setAutoStartup(true);                  // 自动启动
        schedulerFactory.setStartupDelay(20);                   // 延时启动，应用启动成功后在启动
        schedulerFactory.setOverwriteExistingJobs(true);        // 覆盖DB中JOB：true、以数据库中已经存在的为准：false
        schedulerFactory.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        return schedulerFactory;
    }

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public TaskDynmicScheduler getJobDynamicScheduler(SchedulerFactoryBean schedulerFactory) {
        Scheduler scheduler = schedulerFactory.getScheduler();
        TaskDynmicScheduler taskDynmicScheduler = new TaskDynmicScheduler();
        taskDynmicScheduler.setScheduler(scheduler);

        return taskDynmicScheduler;
    }
}
