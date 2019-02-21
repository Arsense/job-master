package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.LogGlue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 18:04
 */
@Mapper
public interface LogGlueMapper {
    public int saveLogGlue(LogGlue logGlue);

    public List<LogGlue> findByTaskId(@Param("jobId") int id);

    public int removeOldTaskById(@Param("jobId") int id, @Param("limit") int limit);

    public int removeById(@Param("jobId") int id);
}
