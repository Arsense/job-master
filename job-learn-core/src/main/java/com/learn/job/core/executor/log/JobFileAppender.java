package com.learn.job.core.executor.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author tangwei
 * @date 2019/2/17 15:50
 */
public class JobFileAppender {
    private static Logger logger = LoggerFactory.getLogger(JobFileAppender.class);
    private static String logBasePath = "/data/applogs/learn-job/jobhandler";
    private static String glueSrcPath = logBasePath.concat("/gluesource");

    // for JobThread (support log for child thread of job handler)
    //public static ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    //主要是用于父线程传递给子线程
    public static final InheritableThreadLocal<String> contextHolder = new InheritableThreadLocal<String>();


    public static void initLogPath(String logPath){
        if (logPath != null && logPath.trim().length() > 0) {
            logBasePath = logPath;
        }
        File logPathDir = new File(logBasePath);
        if (!logPathDir.exists()) {
            logPathDir.mkdirs();
        }
        logBasePath = logPathDir.getPath();
        File glueBaseDir = new File(logPathDir, "gluesource");
        if (!glueBaseDir.exists()) {
            glueBaseDir.mkdirs();
        }
        glueSrcPath = glueBaseDir.getPath();
    }


    /**
     * log filename, like "logPath/yyyy-MM-dd/9999.log"
     *
     * @param triggerDate
     * @param logId
     * @return
     */
    public static String makeLogFileName(Date triggerDate, int logId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	// avoid concurrent problem, can not be static
        File logFilePath = new File(getLogBasePath(), sdf.format(triggerDate));
        if (!logFilePath.exists()) {
            logFilePath.mkdir();
        }

        // filePath/yyyy-MM-dd/9999.log
        String logFileName = logFilePath.getPath()
                .concat(File.separator)
                .concat(String.valueOf(logId))
                .concat(".log");
        return logFileName;
    }

    /**
     * append log
     *
     * @param logFileName
     * @param appendLog
     */
    public static void appendLog(String logFileName, String appendLog) {
        if(logFileName == null ||logFileName.trim().length() == 0) {
            return;
        }
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            }
        }

        // log
        if (appendLog == null) {
            appendLog = "";
        }
        appendLog += "\r\n";

        // append file content
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile, true);
            fos.write(appendLog.getBytes("utf-8"));
            fos.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }



    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        JobFileAppender.logger = logger;
    }

    public static String getLogBasePath() {
        return logBasePath;
    }

    public static void setLogBasePath(String logBasePath) {
        JobFileAppender.logBasePath = logBasePath;
    }

    public static String getGlueSrcPath() {
        return glueSrcPath;
    }

    public static void setGlueSrcPath(String glueSrcPath) {
        JobFileAppender.glueSrcPath = glueSrcPath;
    }
}
