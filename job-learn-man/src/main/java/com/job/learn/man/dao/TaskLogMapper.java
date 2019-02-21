package com.job.learn.man.dao;

import com.learn.job.core.executor.domain.TaskLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/18 16:50
 */
@Mapper
public interface TaskLogMapper {

    public TaskLog loadById(@Param("id") int id);


    public int saveLog(TaskLog taskLog);


    public List<TaskLog> pageList(@Param("offset") int offset,
                                   @Param("pagesize") int pagesize,
                                   @Param("jobGroup") int jobGroup,
                                   @Param("jobId") int jobId,
                                   @Param("triggerTimeStart") Date triggerTimeStart,
                                   @Param("triggerTimeEnd") Date triggerTimeEnd,
                                   @Param("logStatus") int logStatus);
    public int pageCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("jobGroup") int jobGroup,
                             @Param("jobId") int jobId,
                             @Param("triggerTimeStart") Date triggerTimeStart,
                             @Param("triggerTimeEnd") Date triggerTimeEnd,
                             @Param("logStatus") int logStatus);


    public int updateTriggerInfo(TaskLog taskLog);

    public int updateHandleInfo(TaskLog taskLog);

    public int removeById(@Param("jobId") int jobId);

    public int triggerCountByHandleCode(@Param("handleCode") int handleCode);

    public List<Map<String, Object>> triggerCountByDay(@Param("from") Date from,
                                                       @Param("to") Date to);

    public int clearLog(@Param("jobGroup") int jobGroup,
                        @Param("jobId") int taskInfo,
                        @Param("clearBeforeTime") Date clearBeforeTime,
                        @Param("clearBeforeNum") int clearBeforeNum);

    public List<Integer> findFailJobLogIds(@Param("pagesize") int pagesize);

    public int updateAlarmStatus(@Param("logId") int logId,
                                 @Param("oldAlarmStatus") int oldAlarmStatus,
                                 @Param("newAlarmStatus") int newAlarmStatus);
}
