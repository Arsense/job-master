package com.job.learn.man.interceptor;

import com.job.learn.man.util.FreemakerUtil;
import com.job.learn.man.util.I18nUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tangwei
 * @date 2019/2/23 23:02
 */
@Component
public class CookieInterceptor extends HandlerInterceptorAdapter {


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        // static method
        if (modelAndView != null) {
            //添加配置到FreeMakerUtils
            modelAndView.addObject("I18nUtil", FreemakerUtil.generateStaticModel(I18nUtil.class.getName()));
        }
        super.postHandle(request, response, handler, modelAndView);
    }
}
