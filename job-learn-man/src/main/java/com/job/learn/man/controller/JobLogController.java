package com.job.learn.man.controller;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import com.job.learn.man.schedule.TaskDynmicScheduler;
import com.job.learn.man.util.I18nUtil;
import com.learn.job.core.executor.business.BusinessExecutor;
import com.learn.job.core.executor.domain.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/25 14:45
 */
@Controller
@RequestMapping("/joblog")
public class JobLogController {
    private static Logger logger = LoggerFactory.getLogger(JobLogController.class);

    @Resource
    private TaskGroupMapper taskGroupMapper;
    @Resource
    public TaskInfoMapper taskInfoMapper;
    @Resource
    public TaskLogMapper taskLogMapper;

    @RequestMapping
    public String index(Model model, @RequestParam(required = false, defaultValue = "0") Integer jobId) {
        // 执行器列表
        List<TaskGroup> jobGroupList =  taskGroupMapper.findAll();
        model.addAttribute("JobGroupList", jobGroupList);
        // 任务
        if (jobId > 0) {
            TaskInfo jobInfo = taskInfoMapper.loadById(jobId);
            model.addAttribute("jobInfo", jobInfo);
        }

        return "joblog/joblog.index";
    }


    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        int jobGroup, int jobId, int logStatus, String filterTime) {
        // parse param
        Date triggerTimeStart = null;
        Date triggerTimeEnd = null;
        if (StringUtils.isNotBlank(filterTime)) {
            String[] temp = filterTime.split(" - ");
            if (temp.length == 2) {
                try {
                    triggerTimeStart = DateUtils.parseDate(temp[0], new String[]{"yyyy-MM-dd HH:mm:ss"});
                    triggerTimeEnd = DateUtils.parseDate(temp[1], new String[]{"yyyy-MM-dd HH:mm:ss"});
                } catch (ParseException e) {	}
            }
        }
        // page query
        List<TaskLog> list = taskLogMapper.pageList(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd, logStatus);
        int list_count = taskLogMapper.pageCount(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd, logStatus);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }


    @RequestMapping("/getJobsByGroup")
    @ResponseBody
    public Result<List<TaskInfo>> getJobsByGroup(int jobGroup){
        List<TaskInfo> list = taskInfoMapper.findByJobsGroup(jobGroup);
        return new Result<List<TaskInfo>>(list);
    }

    @RequestMapping("/logDetailPage")
    public String logDetailPage(int id, Model model){
        // base check
        Result<String> logStatue = Result.SUCCESS;
        TaskLog jobLog = taskLogMapper.loadById(id);
        if (jobLog == null) {
            throw new RuntimeException(I18nUtil.getString("joblog_logid_unvalid"));
        }

        model.addAttribute("triggerCode", jobLog.getTriggerCode());
        model.addAttribute("handleCode", jobLog.getHandleCode());
        model.addAttribute("executorAddress", jobLog.getExecutorAddress());
        model.addAttribute("triggerTime", jobLog.getTriggerTime().getTime());
        model.addAttribute("logId", jobLog.getId());
        return "joblog/joblog.detail";
    }


    @RequestMapping("/logDetailCat")
    @ResponseBody
    public Result<LogResult> logDetailCat(String executorAddress, long triggerTime, int logId, int fromLineNum){

        BusinessExecutor executorBiz = null;
        try {
            executorBiz = TaskDynmicScheduler.getBusinessExecutor(executorAddress);
            Result<LogResult> logResult = executorBiz.log(triggerTime, logId, fromLineNum);
            return logResult;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            return new Result<LogResult>(Result.FAIL_CODE, e.getMessage());
        }

    }


}
