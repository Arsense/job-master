package com.job.learn.man.controller;

import com.job.learn.man.service.TaskService;
import com.learn.job.core.executor.domain.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
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

//    @RequestMapping("/chartInfo")
//    @ResponseBody
//    public Result<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
//        Result<Map<String, Object>> chartInfo = taskService.chartInfo(startDate, endDate);
//        return chartInfo;
//    }

}
