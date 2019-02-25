package com.job.learn.man.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author tangwei
 * @date 2019/2/25 12:22
 */
@Controller
public class TestFMController {


    @RequestMapping("/test")
    public ModelAndView testFreemaker(ModelAndView model){

        model.addObject("name","clay");
        return model;
    }
}
