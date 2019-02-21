package com.learn.job.core.executor.domain;

import java.io.Serializable;

/**
 * @author tangwei
 * @date 2019/2/17 18:47
 */
public class HandleCallbackParam implements Serializable {

    public HandleCallbackParam(){}
    private int logId;
    private long logDateTime;
    private Result<String> result;

    public HandleCallbackParam(int logId, long logDateTime, Result<String> result) {
        this.logId = logId;
        this.logDateTime = logDateTime;
        this.result = result;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public long getLogDateTime() {
        return logDateTime;
    }

    public void setLogDateTime(long logDateTime) {
        this.logDateTime = logDateTime;
    }

    public Result<String> getResult() {
        return result;
    }

    public void setResult(Result<String> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "HandleCallbackParam{" +
                "logId=" + logId +
                ", logDateTime=" + logDateTime +
                ", result=" + result +
                '}';
    }
}
