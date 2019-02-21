package com.job.learn.man.dao;

import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.TaskLog;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/21 17:02
 */
public class TaskLogMapperTest extends BaseTest {
    @Resource
    private TaskLogMapper taskLogMapper;

    @Test
    public void test(){
        List<TaskLog> list = taskLogMapper.pageList(0, 10, 1, 1, null, null, 1);
        int list_count = taskLogMapper.pageCount(0, 10, 1, 1, null, null, 1);

        TaskLog log = new TaskLog();
        log.setJobGroup(1);
        log.setJobId(1);

        int result1 = taskLogMapper.saveLog(log);
        TaskLog taskLog = taskLogMapper.loadById(log.getId());

        log.setTriggerTime(new Date());
        log.setTriggerCode(1);
        log.setTriggerMsg("1");
        log.setExecutorAddress("1");
        log.setExecutorHandler("1");
        log.setExecutorParam("1");
        result1 = taskLogMapper.updateTriggerInfo(log);
        taskLog = taskLogMapper.loadById(log.getId());


        log.setHandleTime(new Date());
        log.setHandleCode(2);
        log.setHandleMsg("2");
        result1 = taskLogMapper.updateHandleInfo(log);
        taskLog = taskLogMapper.loadById(log.getId());


        List<Map<String, Object>> list2 = taskLogMapper.triggerCountByDay(DateUtils.addDays(new Date(), 30), new Date());

        int ret4 = taskLogMapper.clearLog(1, 1, new Date(), 100);

        int ret2 = taskLogMapper.removeById(log.getJobId());

        int ret3 = taskLogMapper.triggerCountByHandleCode(-1);
    }
}
