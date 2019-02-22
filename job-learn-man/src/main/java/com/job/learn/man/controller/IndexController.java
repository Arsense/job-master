package com.job.learn.man.controller;

import com.job.learn.man.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author tangwei
 * @date 2019/2/22 14:03
 */
@Controller
public class IndexController {

    @Resource
    private TaskService taskService;

    @RequestMapping("/")
    public String index(Model model) {
        Map<String, Object> dashboardMap = taskService.dashboardInfo();
        model.addAllAttributes(dashboardMap);
        return "index";
    }
}
