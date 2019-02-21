package com.learn.job.core.executor;

import com.learn.job.core.executor.thread.JoblogFileCleanThread;
import com.learn.job.core.executor.thread.TaskThread;
import com.learn.job.core.executor.thread.TriggerCallbackThread;
import com.learn.job.core.executor.business.AdminBusiness;
import com.learn.job.core.executor.log.JobFileAppender;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.invoker.route.LoadBalance;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在每个日志文件中存储触发器日志
 * @author tangwei
 * @date 2019/2/17 14:19
 */
public class AbstractJobExecutor {
    //管理端地址
    private String adminAddresses;
    //日子路径
    private String logPath;
    //Token不知规则和来自何方
    private String accessToken;
    private int logRetentionDays;
    private static List<AdminBusiness> adminBusinessList;


    private static final Logger logger = LoggerFactory.getLogger(AbstractJobExecutor.class);
    //JobHandler存储仓库
    private static ConcurrentHashMap<String, AbstractJobHandler> jobHandlerRepository = new ConcurrentHashMap<String, AbstractJobHandler>();
    private static ConcurrentHashMap<Integer, TaskThread> jobThreadRepository = new ConcurrentHashMap<Integer, TaskThread>();

    // ---------------------- start + stop ----------------------

    /**
     * 初始化函数 spring的Bean加载前就会执行
     * @throws Exception
     */
    public void start()  {
        // 初始化触发器的日志文件目录
        JobFileAppender.initLogPath(logPath);
        // init admin-client
        initAdminBusinessList(adminAddresses, accessToken);
        //初始化日志清理工具
        JoblogFileCleanThread.getInstance().start(logRetentionDays);
        // 初始化线程回调
        TriggerCallbackThread.getInstance().start();
    }

    /**添加jobHandler
     *
     * @param name
     * @param jobHandler
     * @return
     */
    public static AbstractJobHandler registJobHandler(String name, AbstractJobHandler jobHandler){
        logger.info(">>>>>>>>>>> xxl-job register jobhandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }
    /**
     * 查询JobHandler
     * @param name
     * @return
     */
    public static AbstractJobHandler loadJobHandler(String name){
        return jobHandlerRepository.get(name);
    }

    private void initAdminBusinessList(String adminAddresses, String accessToken) {
        if (adminAddresses == null || adminAddresses.trim().length() <= 0) {
            return;
        }
        //也是玩的代理呀
        for (String address: adminAddresses.trim().split(",")) {
            if (address!=null && address.trim().length()>0) {
                String addressUrl = address.concat(AdminBusiness.MAPPING);
                //妈呀这个RPC干了啥啊
                AdminBusiness business = (AdminBusiness) new XxlRpcReferenceBean(
                        NetEnum.JETTY,
                        Serializer.SerializeEnum.HESSIAN.getSerializer(),
                        CallType.SYNC,
                        LoadBalance.ROUND,
                        AdminBusiness.class,
                        null,
                        10000,
                        addressUrl,
                        accessToken,
                        null,
                        null
                ).getObject();
                if (adminBusinessList == null) {
                    adminBusinessList = new ArrayList<AdminBusiness>();
                }
                adminBusinessList.add(business);
            }
        }
    }

    /**
     * 注册执行相应的JobThread
     * @param jobId
     * @param handler
     * @param removeOldReason
     * @return
     */
    public static TaskThread registJobThread(int jobId, AbstractJobHandler handler, String removeOldReason){
        TaskThread newJobThread = new TaskThread(jobId, handler);
        newJobThread.start();
        logger.info(">>>>>>>>>>> xxl-job regist JobThread success, jobId:{}, handler:{}", new Object[]{jobId, handler});
        TaskThread oldJobThread = jobThreadRepository.put(jobId, newJobThread);	// putIfAbsent | oh my god, map's put method return the old value!!!
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
        return newJobThread;
    }



    public String getAdminAddresses() {
        return adminAddresses;
    }

    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getLogRetentionDays() {
        return logRetentionDays;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public static List<AdminBusiness> getAdminBusinessList() {
        return adminBusinessList;
    }

    public static void setAdminBusinessList(List<AdminBusiness> adminBusinessList) {
        AbstractJobExecutor.adminBusinessList = adminBusinessList;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static ConcurrentHashMap<String, AbstractJobHandler> getJobHandlerRepository() {
        return jobHandlerRepository;
    }

    public static void setJobHandlerRepository(ConcurrentHashMap<String, AbstractJobHandler> jobHandlerRepository) {
        AbstractJobExecutor.jobHandlerRepository = jobHandlerRepository;
    }

    public static ConcurrentHashMap<Integer, TaskThread> getJobThreadRepository() {
        return jobThreadRepository;
    }
    public static TaskThread loadJobThread(int jobId){
        TaskThread taskThread = jobThreadRepository.get(jobId);
        return taskThread;
    }

    public static void removeJobThread(int jobId, String removeOldReason){
        TaskThread oldJobThread = jobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
    }

}
