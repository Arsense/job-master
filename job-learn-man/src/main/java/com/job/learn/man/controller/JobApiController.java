package com.job.learn.man.controller;

import com.job.learn.man.schedule.TaskDynmicScheduler;
import com.learn.job.core.executor.business.AdminBusiness;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * core端 RPC获取BusinessAdmin 实例接口
 *
 * @author tangwei
 * @date 2019/2/25 14:46
 */
@Controller
public class JobApiController  implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @RequestMapping(AdminBusiness.MAPPING)
//    @PermessionLimit(limit=false)
    public void api(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        TaskDynmicScheduler.invokeAdminService(request, response);
    }
}
