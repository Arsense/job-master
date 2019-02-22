package com.job.learn.man.service.impl;

import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import com.job.learn.man.service.TaskService;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TaskLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/22 14:04
 */
@Service
public class TaskServiceImpl implements TaskService {
    private static Logger logger = LoggerFactory.getLogger(TaskService.class);


    @Resource
    private TaskInfoMapper taskInfoMapper;

    @Resource
    private TaskLogMapper taskLogMapper;


    @Override
    public Map<String, Object> dashboardInfo() {
        int jobInfoCount = taskInfoMapper.findAllCount();
        int jobLogCount = taskLogMapper.triggerCountByHandleCode(-1);
        int jobLogSuccessCount = taskLogMapper.triggerCountByHandleCode(Result.SUCCESS_CODE);

        Map<String, Object> dashboardMap = new HashMap<String, Object>();
        dashboardMap.put("jobInfoCount", jobInfoCount);
        dashboardMap.put("jobLogCount", jobLogCount);
        dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
//        dashboardMap.put("executorCount", executorCount);
        return dashboardMap;


    }
}
