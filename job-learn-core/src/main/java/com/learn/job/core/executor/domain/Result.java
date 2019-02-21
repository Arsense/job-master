package com.learn.job.core.executor.domain;

import java.io.Serializable;

/**
 * @author tangwei
 * @date 2019/2/17 18:55
 */
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;


    public static final Result<String> SUCCESS = new Result<String>(null);
    public static final Result<String> FAIL = new Result<String>(FAIL_CODE, null);

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(T data) {
        this.code = SUCCESS_CODE;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
