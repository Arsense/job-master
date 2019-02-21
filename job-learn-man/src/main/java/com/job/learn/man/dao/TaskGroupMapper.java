package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author tangwei
 * @date 2019/2/19 15:15
 */
@Mapper
public interface TaskGroupMapper {
    public TaskGroup loadById(@Param("id") int id);

}
