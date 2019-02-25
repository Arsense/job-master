package com.job.learn.man.service.impl;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import com.job.learn.man.domain.ExecutorRouteStrategyEnum;
import com.job.learn.man.schedule.TaskDynmicScheduler;
import com.job.learn.man.service.TaskService;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TaskGroup;
import com.learn.job.core.executor.domain.TaskInfo;
import com.learn.job.core.executor.domain.TaskLog;
import com.learn.job.core.executor.enums.GlueTypeEnum;
import com.learn.job.core.executor.route.ExecutorBlockStrategyEnum;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
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
    @Resource
    private TaskGroupMapper taskGroupMapper;


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

    @Override
    public Result<String> add(TaskInfo jobInfo) {
        TaskGroup group = taskGroupMapper.loadById(jobInfo.getJobGroup());

        if (group == null) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("system_please_choose")+I18nUtil.getString("jobinfo_field_jobgroup")) );
        }
        if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
            return new Result<String>(Result.FAIL_CODE, I18nUtil.getString("jobinfo_field_cron_unvalid") );
        }
        if (StringUtils.isBlank(jobInfo.getJobDesc())) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_jobdesc")) );
        }
        if (StringUtils.isBlank(jobInfo.getAuthor())) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("system_please_input")+I18nUtil.getString("jobinfo_field_author")) );
        }
        if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorRouteStrategy")+I18nUtil.getString("system_unvalid")) );
        }
        if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("jobinfo_field_executorBlockStrategy")+I18nUtil.getString("system_unvalid")) );
        }
        if (GlueTypeEnum.match(jobInfo.getGlueType()) == null) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("jobinfo_field_gluetype")+I18nUtil.getString("system_unvalid")) );
        }
        if (GlueTypeEnum.BEAN==GlueTypeEnum.match(jobInfo.getGlueType()) && StringUtils.isBlank(jobInfo.getExecutorHandler())) {
            return new Result<String>(Result.FAIL_CODE, (I18nUtil.getString("system_please_input")+"JobHandler") );
        }
        return null;
    }

    @Override
    public Result<String> update(TaskInfo jobInfo) {
        return null;
    }

    @Override
    public Result<String> remove(int id) throws SchedulerException {
        TaskInfo taskInfo = taskInfoMapper.loadById(id);
        String group = String.valueOf(taskInfo.getJobGroup());
        String name = String.valueOf(taskInfo.getId());

        // unbind quartz
        TaskDynmicScheduler.removeJob(name, group);

        taskInfoMapper.removeById(id);
        taskInfoMapper.removeById(id);
        taskGroupMapper.remove(id);
        return Result.SUCCESS;
    }

    @Override
    public Result<String> start(int id) {
        TaskInfo xxlJobInfo = taskInfoMapper.loadById(id);

        String group = String.valueOf(xxlJobInfo.getJobGroup());
        String name = String.valueOf(xxlJobInfo.getId());
        String cronExpression = xxlJobInfo.getJobCron();

        try {
            boolean result = TaskDynmicScheduler.addJob(name, group, cronExpression);
            return result?Result.SUCCESS:Result.FAIL;
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return Result.FAIL;
        }
    }

    @Override
    public Result<String> stop(int id) {
        return null;
    }
}
