package com.learn.job.core.executor;

import com.learn.job.core.executor.anotation.JobHandler;
import com.learn.job.core.executor.glue.GlueFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 *
 * @author tangwei
 * @date 2019/2/17 1:09
 */
public class JobSpringExecutor extends AbstractJobExecutor implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void start() throws Exception {
        //初始化jobHandler 库
        initJobHandlerRepository(applicationContext);

        // 初始化为SpringGule的实例
        GlueFactory.refreshInstance(1);

        super.start();
    }


    private void destroy() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    private void initJobHandlerRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        //获取所有handler的实例
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(JobHandler.class);
        if (serviceBeanMap == null || serviceBeanMap.size() <= 0) {
            return;
        }
        //遍历注册相应的Handler
        for (Object serviceBean : serviceBeanMap.values()) {
            if(serviceBean instanceof AbstractJobHandler) {
                String name = serviceBean.getClass().getAnnotation(JobHandler.class).value();
                AbstractJobHandler handler = (AbstractJobHandler) serviceBean;
                //重复加载
                if (loadJobHandler(name) != null) {
                    throw new RuntimeException("xxl-job jobhandler naming conflicts.");
                }
                registJobHandler(name, handler);
            }
        }

    }


}
