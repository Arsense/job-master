package com.job.learn.man.dao;

import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.LogGlue;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 17:03
 */
public class TaskLogGlueMapperTest extends BaseTest {

    @Resource
    private LogGlueMapper logGlueMapper;

    @Test
    public void test(){
        LogGlue logGlue = new LogGlue();
        logGlue.setJobId(1);
        logGlue.setGlueType("1");
        logGlue.setGlueSource("1");
        logGlue.setGlueRemark("1");
        int result = logGlueMapper.saveLogGlue(logGlue);

        List<LogGlue> glues = logGlueMapper.findByTaskId(1);

        int result2 = logGlueMapper.removeOldTaskById(1, 1);

        int result3 =logGlueMapper.removeById(1);
    }
}
