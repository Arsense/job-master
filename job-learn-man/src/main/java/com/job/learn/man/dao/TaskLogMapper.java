package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author tangwei
 * @date 2019/2/18 16:50
 */
@Mapper
public interface TaskLogMapper {

    public TaskLog load(@Param("id") int id);


    public int saveLog(TaskLog taskLog);
}
