package com.job.learn.man.controller;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.domain.ExecutorRouteStrategyEnum;
import com.job.learn.man.service.TaskService;
import com.job.learn.man.thread.TaskTriggerPoolHelper;
import com.job.learn.man.trigger.TriggerTypeEnum;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.domain.TaskGroup;
import com.learn.job.core.executor.domain.TaskInfo;
import com.learn.job.core.executor.enums.GlueTypeEnum;
import com.learn.job.core.executor.route.ExecutorBlockStrategyEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/24 13:16
 */
@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {

    @Resource
    private TaskGroupMapper taskGroupMapper;
    @Resource
    private TaskService taskService;

    @RequestMapping
    public String index(Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

        // 枚举-字典
        model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());	// 路由策略-列表
        model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());								// Glue类型-字典
        model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());	// 阻塞处理策略-字典

        // 任务组
        List<TaskGroup> jobGroupList =  taskGroupMapper.findAll();
        model.addAttribute("JobGroupList", jobGroupList);
        model.addAttribute("jobGroup", jobGroup);

        return "jobinfo/jobinfo.index";
    }

    @RequestMapping("/add")
    @ResponseBody
    public Result<String> add(TaskInfo jobInfo) {
        return taskService.add(jobInfo);
    }

    @RequestMapping("/update")
    @ResponseBody
    public Result<String> update(TaskInfo jobInfo) {
        return taskService.update(jobInfo);
    }

    @RequestMapping("/remove")
    @ResponseBody
    public Result<String> remove(int id) {
        return taskService.remove(id);
    }

    @RequestMapping("/stop")		// TODO, pause >> stop
    @ResponseBody
    public Result<String> pause(int id) {
        return taskService.stop(id);
    }

    @RequestMapping("/start")		// TODO, resume >> start
    @ResponseBody
    public Result<String> start(int id) {
        return taskService.start(id);
    }

    @RequestMapping("/trigger")
    @ResponseBody
    //@PermessionLimit(limit = false)
    public Result<String> triggerJob(int id, String executorParam) {
        // force cover job param
        if (executorParam == null) {
            executorParam = "";
        }

        TaskTriggerPoolHelper.trigger(id, TriggerTypeEnum.MANUAL, -1, null, executorParam);
        return Result.SUCCESS;
    }
}
