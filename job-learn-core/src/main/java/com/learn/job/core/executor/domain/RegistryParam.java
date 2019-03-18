package com.learn.job.core.executor.domain;

import java.io.Serializable;

/**
 * @author tangwei
 * @date 2019/2/26 11:33
 */
public class RegistryParam implements Serializable {
    private String registGroup;
    private String registryKey;
    private String registryValue;

    public RegistryParam(){}
    public RegistryParam(String registGroup, String registryKey, String registryValue) {
        this.registGroup = registGroup;
        this.registryKey = registryKey;
        this.registryValue = registryValue;
    }

    public String getRegistGroup() {
        return registGroup;
    }

    public void setRegistGroup(String registGroup) {
        this.registGroup = registGroup;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public void setRegistryKey(String registryKey) {
        this.registryKey = registryKey;
    }

    public String getRegistryValue() {
        return registryValue;
    }

    public void setRegistryValue(String registryValue) {
        this.registryValue = registryValue;
    }

    @Override
    public String toString() {
        return "RegistryParam{" +
                "registGroup='" + registGroup + '\'' +
                ", registryKey='" + registryKey + '\'' +
                ", registryValue='" + registryValue + '\'' +
                '}';
    }
}
