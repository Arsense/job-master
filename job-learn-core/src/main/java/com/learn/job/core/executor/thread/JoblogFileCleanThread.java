package com.learn.job.core.executor.thread;

import com.learn.job.core.executor.log.JobFileAppender;
import com.learn.job.core.executor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author tangwei
 * @date 2019/2/17 16:37
 */
public class JoblogFileCleanThread {

    private static Logger logger = LoggerFactory.getLogger(JoblogFileCleanThread.class);
    private static JoblogFileCleanThread instance = new JoblogFileCleanThread();
    private Thread localThread;
    //终止标志
    private volatile boolean toStop = false;

    public static JoblogFileCleanThread getInstance(){
        return instance;
    }

    public void start(final long logKeepDays){
        // limit min value
        if (logKeepDays < 3 ) {
            return;
        }

        localThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStop) {
                    try {
                        File[] childDirs = new File(JobFileAppender.getLogBasePath()).listFiles();
                        if (childDirs != null && childDirs.length > 0) {
                            Calendar todayCal = Calendar.getInstance();
                            todayCal.set(Calendar.HOUR_OF_DAY,0);
                            todayCal.set(Calendar.MINUTE,0);
                            todayCal.set(Calendar.SECOND,0);
                            todayCal.set(Calendar.MILLISECOND,0);

                            Date todayDate = todayCal.getTime();
                            for (File childFile: childDirs) {
                                // valid
                                if (!childFile.isDirectory()) {
                                    continue;
                                }
                                if (childFile.getName().indexOf("-") == -1) {
                                    continue;
                                }

                                // file create date
                                Date logFileCreateDate = null;
                                try {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                                } catch (ParseException e) {
                                    logger.error(e.getMessage(), e);
                                }
                                if (logFileCreateDate == null) {
                                    continue;
                                }

                                if ((todayDate.getTime()-logFileCreateDate.getTime()) >= logKeepDays * (24 * 60 * 60 * 1000) ) {
                                    //删除日志文件
                                    FileUtil.deleteKeeply(childFile);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.DAYS.sleep(1);
                    } catch (InterruptedException e) {
                        if (!toStop) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-job, executor JobLogFileCleanThread thread destory.");
            }
        });
        //设置成后台进程
        localThread.setDaemon(true);
        localThread.start();
    }
}
