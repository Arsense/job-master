package com.learn.job.core.executor.thread;

import com.learn.job.core.executor.AbstractJobExecutor;
import com.learn.job.core.executor.business.AdminBusiness;
import com.learn.job.core.executor.domain.HandleCallbackParam;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.log.JobFileAppender;
import com.learn.job.core.executor.log.JobLogger;
import com.learn.job.core.executor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author tangwei
 * @date 2019/2/17 17:32
 */
public class TriggerCallbackThread   {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private static TriggerCallbackThread instance = new TriggerCallbackThread();
    public static TriggerCallbackThread getInstance(){
        return instance;
    }
    private Thread triggerCallbackThread;

//    private thread triggerRetryCallbackThread;

    /**
     * job results callback queue  job要按顺序执行阻塞队列更好
     */
    private LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<HandleCallbackParam>();

    private volatile boolean toStop = false;

    /**
     *  起始运行函数
     */
    public void start() {
        if (AbstractJobExecutor.getAdminBusinessList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }
        triggerCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!toStop){
                    try {
                        HandleCallbackParam callback = getInstance().callBackQueue.take();
                        if (callback != null) {
                            // callback list param
                            List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                            int drainToNum = getInstance().callBackQueue.drainTo(callbackParamList);
                            callbackParamList.add(callback);

                            // callback, will retry if error
                            if (callbackParamList != null && callbackParamList.size() > 0) {
                                doCallback(callbackParamList);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }

    /**
     * callback核心功能 有重复尝试机制
     * @param callbackParamList
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList){
        boolean retry = false;
        //失败的callback添加到失败队列
        for (AdminBusiness business: AbstractJobExecutor.getAdminBusinessList()) {
            try {
                Result<String> callbackResult = business.callback(callbackParamList);
                if (callbackResult!= null && Result.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    retry = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!retry) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam: callbackParamList) {
            String logFileName = JobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTime()), callbackParam.getLogId());
            JobFileAppender.contextHolder.set(logFileName);
            JobLogger.log(logContent);
        }

    }

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
//        // append file
//        String content = JacksonUtil.writeValueAsString(callbackParamList);
//        FileUtil.appendFileLine(failCallbackFileName, content);
    }
}
