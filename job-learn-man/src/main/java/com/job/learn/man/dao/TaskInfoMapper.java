package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author tangwei
 * @date 2019/2/18 18:27
 */
@Mapper
public interface TaskInfoMapper {
    public TaskInfo findById(@Param("id") int id);
}
