package com.learn.job.core.executor.thread;

import com.learn.job.core.executor.AbstractJobExecutor;
import com.learn.job.core.executor.business.AdminBusiness;
import com.learn.job.core.executor.domain.RegistryParam;
import com.learn.job.core.executor.domain.Result;
import com.learn.job.core.executor.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author tangwei
 * @date 2019/2/26 10:59
 */
public class ExecutorRegistryThread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();
    public static ExecutorRegistryThread getInstance(){
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;
    public void start(final String appName, final String address){
        // valid
        if (appName==null || appName.trim().length()==0) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, appName is null.");
            return;
        }
        if (AbstractJobExecutor.getAdminBusinessList() == null) {
            logger.warn(">>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // registry
                    while (!toStop) {
                        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appName, address);
                        for (AdminBusiness adminBusiness: AbstractJobExecutor.getAdminBusinessList()) {
                            try {
                                if (registryParam != null) {
                                    logger.info("=======这里是" + registryParam);
                                }
                                Result<String> registryResult = adminBusiness.registry(registryParam);
                                if (registryResult != null && Result.SUCCESS_CODE == registryResult.getCode()) {
                                    registryResult = Result.SUCCESS;
                                    logger.info(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                    break;
                                } else {
                                    logger.info(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                }
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);

                            }
                        }
                    }

                    //where 是扫描注册上当前的执行器 执行stop后 移除当前的执行器
                    try {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            logger.warn(">>>>>>>>>>> xxl-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
                // registry remove
                try {
                    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appName, address);
                    for (AdminBusiness adminBiz: AbstractJobExecutor.getAdminBusinessList()) {
                        try {
                            Result<String> registryResult = adminBiz.registryRemove(registryParam);
                            if (registryResult!=null && Result.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = Result.SUCCESS;
                                logger.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                logger.info(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Exception e) {
                            logger.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
                        }

                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info(">>>>>>>>>>> xxl-job, executor registry thread destory.");

            }
        });
        //设置后台线程
        registryThread.setDaemon(true);
        registryThread.start();
    }


    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
