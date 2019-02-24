package com.job.learn.man.service;


import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TaskInfo;

import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/22 13:58
 */
public interface TaskService {

    /**
     * dashboard info
     *
     * @return
     */
    public Map<String,Object> dashboardInfo();


    /**
     * 添加JOB
     *
     * @param jobInfo
     * @return
     */
    public Result<String> add(TaskInfo jobInfo);

    /**
     * 更新任务执行信息
     *
     * @param jobInfo
     * @return
     */
    public Result<String> update(TaskInfo jobInfo);

    /**
     * remove job, unbind quartz
     *
     * @param id
     * @return
     */
    public Result<String> remove(int id);

    /**
     * 执行JOB 绑定quartz
     *
     * @param id
     * @return
     */
    public Result<String> start(int id);

    /**
     * 结束JOB 解除绑定
     *
     * @param id
     * @return
     */
    public Result<String> stop(int id);


}
