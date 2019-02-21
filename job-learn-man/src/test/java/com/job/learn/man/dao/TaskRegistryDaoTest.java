package com.job.learn.man.dao;

import com.job.learn.man.BaseTest;
import com.learn.job.core.executor.domain.TaskRegistry;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/21 17:02
 */
public class TaskRegistryDaoTest extends BaseTest {

    @Resource
    private TaskRegistryMapper taskRegistryMapper;

    @Test
    public void test(){
        int result = taskRegistryMapper.registryUpdate("g1", "k1", "v1");
        if (result < 1) {
            result = taskRegistryMapper.registrySave("g1", "k1", "v1");
        }

        List<TaskRegistry> list = taskRegistryMapper.findAll(1);

        int result2 = taskRegistryMapper.removeDead(1);
    }

}
