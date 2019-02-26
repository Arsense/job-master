package com.job.learn.man.service;

import com.job.learn.man.dao.TaskInfoMapper;
import com.job.learn.man.dao.TaskLogMapper;
import com.job.learn.man.dao.TaskRegistryMapper;
import com.job.learn.man.trigger.TriggerTypeEnum;
import com.learn.job.core.executor.AbstractJobHandler;
import com.learn.job.core.executor.business.AdminBusiness;
import com.learn.job.core.executor.domain.*;
import com.job.learn.man.thread.TaskTriggerPoolHelper;
import com.job.learn.man.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * @author tangwei
 * @date 2019/2/17 18:59
 */
@Service
public class AdminBusinessService implements AdminBusiness {

    private static Logger logger = LoggerFactory.getLogger(AdminBusinessService.class);

    @Resource
    private TaskLogMapper taskLogMapper;
    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private TaskRegistryMapper taskRegistryMapper;


    public Result<String> callback(List<HandleCallbackParam> callbackParamList) {
        //在这里把参数传过来调用
        for (HandleCallbackParam handleCallbackParam: callbackParamList) {
            Result<String> callbackResult = callback(handleCallbackParam);
            logger.info(">>>>>>>>> JobApiController.callback {}, handleCallbackParam={}, callbackResult={}"+
                    (callbackResult.getCode()== AbstractJobHandler.SUCCESS.getCode()?"success":"fail"), handleCallbackParam, callbackResult);
        }
            return null;
    }

    @Override
    public Result<String> registry(RegistryParam registryParam) {
        int result = taskRegistryMapper.registryUpdate(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        if (result < 1) {
            taskRegistryMapper.registrySave(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        }
        return Result.SUCCESS;
    }

    @Override
    public Result<String> registryRemove(RegistryParam registryParam) {
        taskRegistryMapper.registryDelete(registryParam.getRegistGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
        return Result.SUCCESS;
    }

    /**
     * 遍历调用
     * @param handleCallbackParam
     * @return
     */
    private Result<String> callback(HandleCallbackParam handleCallbackParam) {
        //通过LogId来处理的
        TaskLog log = taskLogMapper.loadById(handleCallbackParam.getLogId());
        if (log == null) {
            return new Result<String>(Result.FAIL_CODE, "log item not found.");
        }
        if (log.getHandleCode() > 0) {
            return new Result<String>(Result.FAIL_CODE, "log repeate callback.");
        }
        // trigger success, to trigger child job  触发其子JOB
        StringBuilder callbackMsg = null;
        if (AbstractJobHandler.SUCCESS.getCode() == handleCallbackParam.getResult().getCode()) {
            TaskInfo taskInfo = taskInfoMapper.findById(log.getJobId());
            if (taskInfo != null && StringUtils.isNotBlank(taskInfo.getChildJobId())) {
                callbackMsg = new StringBuilder("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_child_run") + "<<<<<<<<<<< </span><br>");

                String[] childJobIds = taskInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (StringUtils.isNotBlank(childJobIds[i]) && StringUtils.isNumeric(childJobIds[i]))?Integer.valueOf(childJobIds[i]):-1;
                    if (childJobId > 0) {

                        TaskTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null);
                        Result<String> triggerChildResult = Result.SUCCESS;

                        // add msg
                        callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"),
                                (i + 1),
                                childJobIds.length,
                                childJobIds[i],
                                (triggerChildResult.getCode() == Result.SUCCESS_CODE ? I18nUtil.getString("system_success") : I18nUtil.getString("system_fail")),
                                triggerChildResult.getMessage()));
                    } else {
                        callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"),
                                (i + 1),
                                childJobIds.length,
                                childJobIds[i]));
                    }
                }

            }
        }
        return null;
    }
}
