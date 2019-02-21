package com.job.learn.man.dao;

import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.TaskInfo;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 17:01
 */
public class TaskInfoMapperTest extends BaseTest {



    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Test
    public void pageList(){
        List<TaskInfo> list = taskInfoMapper.pageList(0, 20, 0, null, null);
        int list_count = taskInfoMapper.pageListCount(0, 20, 0, null, null);

        System.out.println(list);
        System.out.println(list_count);

        List<TaskInfo> list2 = taskInfoMapper.findByJobsGroup(1);

    }

    @Test
    public void save_load(){
        TaskInfo info = new TaskInfo();
        info.setJobGroup(1);
        info.setJobCron("jobCron");
        info.setJobDesc("desc");
        info.setAuthor("setAuthor");
        info.setAlarmEmail("setAlarmEmail");
        info.setExecutorRouteStrategy("setExecutorRouteStrategy");
        info.setExecutorHandler("setExecutorHandler");
        info.setExecutorParam("setExecutorParam");
        info.setExecutorBlockStrategy("setExecutorBlockStrategy");
        info.setGlueType("setGlueType");
        info.setGlueSource("setGlueSource");
        info.setGlueRemark("setGlueRemark");
        info.setChildJobId("1");

        int count = taskInfoMapper.saveInfo(info);

        TaskInfo info2 = taskInfoMapper.loadById(info.getId());
        info2.setJobCron("jobCron2");
        info2.setJobDesc("desc2");
        info2.setAuthor("setAuthor2");
        info2.setAlarmEmail("setAlarmEmail2");
        info2.setExecutorRouteStrategy("setExecutorRouteStrategy2");
        info2.setExecutorHandler("setExecutorHandler2");
        info2.setExecutorParam("setExecutorParam2");
        info2.setExecutorBlockStrategy("setExecutorBlockStrategy2");
        info2.setGlueType("setGlueType2");
        info2.setGlueSource("setGlueSource2");
        info2.setGlueRemark("setGlueRemark2");
        info2.setGlueUpdatetime(new Date());
        info2.setChildJobId("1");

        int item2 = taskInfoMapper.updateInfo(info2);

        taskInfoMapper.removeById(info2.getId());

        List<TaskInfo> infoGroup = taskInfoMapper.findByJobsGroup(1);

        int result3 = taskInfoMapper.findAllCount();

        System.out.println("----debug------");

    }



}
