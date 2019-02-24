package com.learn.job.core.executor.enums;

/**
 * @author tangwei
 * @date 2019/2/24 19:36
 */
public class RegistryConfig {
    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistType{ EXECUTOR, ADMIN }

}
