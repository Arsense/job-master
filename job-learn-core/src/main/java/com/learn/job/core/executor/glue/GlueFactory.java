package com.learn.job.core.executor.glue;

import com.learn.job.core.executor.AbstractJobHandler;
import groovy.lang.GroovyClassLoader;

/**
 * @author tangwei
 * @date 2019/2/17 14:26
 */
public class GlueFactory {

    //工厂模式
    private static GlueFactory glueFactory = new GlueFactory();
    public static GlueFactory getInstance(){
        return glueFactory;
    }
    public static void refreshInstance(int type){
        if (type == 0) {
            glueFactory = new GlueFactory();
        } else if (type == 1) {
            glueFactory = new SpringGlueFactory();
        }
    }
    /**
     * groovy class loader  一种动态语言 这里用来执行脚本
     */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();


    public AbstractJobHandler loadNewInstance(String codeSource) throws Exception{
        if (codeSource!=null && codeSource.trim().length()>0) {
            Class<?> clazz = groovyClassLoader.parseClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance!=null) {
                    if (instance instanceof AbstractJobHandler) {
                        this.injectService(instance);
                        return (AbstractJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
                                + "cannot convert from instance["+ instance.getClass() +"] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }



    /**
     * inject service of bean field
     *
     * @param instance
     */
    public void injectService(Object instance) {
        // do something
    }
}
