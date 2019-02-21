package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 18:04
 */
@Mapper
public interface TaskRegistryMapper {
    public int removeDead(@Param("timeout") int timeout);

    public List<TaskRegistry> findAll(@Param("timeout") int timeout);

    public int registryUpdate(@Param("registryGroup") String registryGroup,
                              @Param("registryKey") String registryKey,
                              @Param("registryValue") String registryValue);

    public int registrySave(@Param("registryGroup") String registryGroup,
                            @Param("registryKey") String registryKey,
                            @Param("registryValue") String registryValue);

    public int registryDelete(@Param("registryGroup") String registGroup,
                              @Param("registryKey") String registryKey,
                              @Param("registryValue") String registryValue);
}
