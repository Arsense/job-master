package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/18 18:27
 */
@Mapper
public interface TaskInfoMapper {
    public TaskInfo findById(@Param("id") int id);


    public List<TaskInfo> pageList(@Param("offset") int offset,
                                     @Param("pagesize") int pagesize,
                                     @Param("jobGroup") int group,
                                     @Param("jobDesc") String description,
                                     @Param("executorHandler") String handler);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobGroup") int group,
                             @Param("jobDesc") String description,
                             @Param("executorHandler") String handler);

    public int saveInfo(TaskInfo info);

    public TaskInfo loadById(@Param("id") int id);

    public int updateInfo(TaskInfo item);

    public int removeById(@Param("id") int id);

    public List<TaskInfo> findByJobsGroup(@Param("jobGroup") int jobGroup);

    public int findAllCount();

}
