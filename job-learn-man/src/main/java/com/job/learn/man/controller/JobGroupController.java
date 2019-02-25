package com.job.learn.man.controller;

import com.job.learn.man.dao.TaskGroupMapper;
import com.job.learn.man.dao.TaskInfoMapper;
import com.learn.job.core.executor.domain.TaskGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author tangwei
 * @date 2019/2/25 14:45
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    @Resource
    public TaskInfoMapper taskInfoMapper;
    @Resource
    public TaskGroupMapper taskGroupMapper;

    @RequestMapping
    public String index(Model model) {
        // job group (executor)
        List<TaskGroup> list = taskGroupMapper.findAll();

        model.addAttribute("list", list);
        return "jobgroup/jobgroup.index";
    }



}
