package com.learn.job.exector.spring.jobhandler;


import com.learn.job.core.executor.AbstractJobHandler;
import com.learn.job.core.executor.anotation.JobHandler;
import com.learn.job.core.executor.domain.Result;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务Handler示例（Bean模式）
 *
 * 开发步骤：
 * 1、继承"IJobHandler"：“com.xxl.job.core.handler.IJobHandler”；
 * 2、注册到Spring容器：添加“@Component”注解，被Spring容器扫描为Bean实例；
 * 3、注册到执行器工厂：添加“@JobHandler(value="自定义jobhandler名称")”注解，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 4、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 * @author tangwei
 * @date 2019/2/17 1:05
 */
@Component
@JobHandler(value="demoJobHandler")
public class DemoJobHandler extends AbstractJobHandler {
    @Override
    public Result<String> execute(String param) throws Exception {
    //        TaskLogger.log("XXL-JOB, Hello World.");
    //
    //        for (int i = 0; i < 5; i++) {
    //            TaskLogger.log("beat at:" + i);
    //            TimeUnit.SECONDS.sleep(2);
    //        }
            return SUCCESS;
    }
}
