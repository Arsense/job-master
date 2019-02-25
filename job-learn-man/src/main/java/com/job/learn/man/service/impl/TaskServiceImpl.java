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
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

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
    public Map<String, Object> pageList(int start, int length, int jobGroup, String jobDesc, String executorHandler, String filterTime) {
        // page list
        List<TaskInfo> list = taskInfoMapper.pageList(start, length, jobGroup, jobDesc, executorHandler);
        int list_count = taskInfoMapper.pageListCount(start, length, jobGroup, jobDesc, executorHandler);

        // fill job info
        if (list!=null && list.size()>0) {
            for (TaskInfo jobInfo : list) {
                //逐个触发了
                TaskDynmicScheduler.fillJobInfo(jobInfo);
            }
        }

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @Override
    public Map<String, Object> dashboardInfo() {
        int jobInfoCount = taskInfoMapper.findAllCount();
        int jobLogCount = taskLogMapper.triggerCountByHandleCode(-1);
        int jobLogSuccessCount = taskLogMapper.triggerCountByHandleCode(Result.SUCCESS_CODE);
        //执行器数量 可以知道自己的执行器起来了木有
        Set<String> executerAddressSet = new HashSet<String>();
        List<TaskGroup> groupList = taskGroupMapper.findAll();

        if (groupList!=null && !groupList.isEmpty()) {
            for (TaskGroup group: groupList) {
                if (group.getRegistryList()!=null && !group.getRegistryList().isEmpty()) {
                    executerAddressSet.addAll(group.getRegistryList());
                }
            }
        }

        int executorCount = executerAddressSet.size();

        Map<String, Object> dashboardMap = new HashMap<String, Object>();
        dashboardMap.put("jobInfoCount", jobInfoCount);
        dashboardMap.put("jobLogCount", jobLogCount);
        dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);

        dashboardMap.put("executorCount", executorCount);
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

    @Override
    public Result<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
        List<String> triggerDayList = new ArrayList<String>();
        List<Integer> triggerDayCountRunningList = new ArrayList<Integer>();
        List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
        List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
        int triggerCountRunningTotal = 0;
        int triggerCountSucTotal = 0;
        int triggerCountFailTotal = 0;

        List<Map<String, Object>> triggerCountMapAll = taskLogMapper.triggerCountByDay(startDate, endDate);
        if (triggerCountMapAll!=null && triggerCountMapAll.size()>0) {
            for (Map<String, Object> item: triggerCountMapAll) {
                String day = String.valueOf(item.get("triggerDay"));
                int triggerDayCount = Integer.valueOf(String.valueOf(item.get("triggerDayCount")));
                int triggerDayCountRunning = Integer.valueOf(String.valueOf(item.get("triggerDayCountRunning")));
                int triggerDayCountSuc = Integer.valueOf(String.valueOf(item.get("triggerDayCountSuc")));
                int triggerDayCountFail = triggerDayCount - triggerDayCountRunning - triggerDayCountSuc;

                triggerDayList.add(day);
                triggerDayCountRunningList.add(triggerDayCountRunning);
                triggerDayCountSucList.add(triggerDayCountSuc);
                triggerDayCountFailList.add(triggerDayCountFail);

                triggerCountRunningTotal += triggerDayCountRunning;
                triggerCountSucTotal += triggerDayCountSuc;
                triggerCountFailTotal += triggerDayCountFail;
            }
        } else {
            for (int i = 4; i > -1; i--) {
                triggerDayList.add(FastDateFormat.getInstance("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -i)));
                triggerDayCountRunningList.add(0);
                triggerDayCountSucList.add(0);
                triggerDayCountFailList.add(0);
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("triggerDayList", triggerDayList);
        result.put("triggerDayCountRunningList", triggerDayCountRunningList);
        result.put("triggerDayCountSucList", triggerDayCountSucList);
        result.put("triggerDayCountFailList", triggerDayCountFailList);

        result.put("triggerCountRunningTotal", triggerCountRunningTotal);
        result.put("triggerCountSucTotal", triggerCountSucTotal);
        result.put("triggerCountFailTotal", triggerCountFailTotal);

		/*// set cache
		LocalCacheUtil.set(cacheKey, result, 60*1000);     // cache 60s*/

        return new Result<Map<String, Object>>(result);
    }
}
