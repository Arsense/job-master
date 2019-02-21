package com.job.learn.man.dao;


import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.TaskGroup;
import com.learn.job.core.executor.domain.TaskInfo;
import org.json.JSONObject;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 17:02
 */
public class TaskGroupMapperTest extends BaseTest {

    @Resource
    private TaskGroupMapper taskGroupMapper;


    @Test
    public void test() {
        List<TaskGroup> list = taskGroupMapper.findAll();

        List<TaskGroup> list2 = taskGroupMapper.findByAddressType(0);

        TaskGroup group = new TaskGroup();
        group.setAppName("setAppName");
        group.setTitle("setTitle");
        group.setOrder(1);
        group.setAddressType(0);
        group.setAddressList("setAddressList");

        int result = taskGroupMapper.saveTaskGroup(group);

        TaskGroup group2 = new TaskGroup();
        group2 = taskGroupMapper.loadById(group.getId());
        group2.setAppName("setAppName2");
        group2.setTitle("setTitle2");
        group2.setOrder(2);
        group2.setAddressType(2);
        group2.setAddressList("setAddressList2");
        int result2 = taskGroupMapper.updateTaskGroup(group2);

        int ret3 = taskGroupMapper.remove(group.getId());
        System.out.println("结果是");
    }

}
