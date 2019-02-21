package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/19 15:15
 */
@Mapper
public interface TaskGroupMapper {
    //根据ID 加载相关任务集群
    public TaskGroup loadById(@Param("id") int id);

    public List<TaskGroup> findAll();

    public List<TaskGroup> findByAddressType(@Param("addressType") int addressType);

    public int saveTaskGroup(TaskGroup taskGroup);

    public int updateTaskGroup(TaskGroup taskGroup);

    public int remove(@Param("id") int id);


}
