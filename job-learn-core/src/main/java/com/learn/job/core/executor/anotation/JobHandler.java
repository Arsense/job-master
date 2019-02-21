package com.learn.job.core.executor.anotation;

import java.lang.annotation.*;

/**
 * @author tangwei
 * @date 2019/2/17 14:13
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandler {

    String value() default "";
}
