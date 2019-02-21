package com.job.learn.man.dao;


import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.TaskGroup;
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

        System.out.println("结果是");
    }

}
