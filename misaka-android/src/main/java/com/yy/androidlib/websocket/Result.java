package com.yy.androidlib.websocket;

public class Result<T> {

    private int code;
    private String message;
    private T result;

    public Result() {
    }

    public Result(String message, int code, T result) {
        this.message = message;
        this.code = code;
        this.result = result;
    }

    public Result(T result) {
        this.code = 1;
        this.message = "success";
        this.result = result;
    }

    public static Result successResult(String destination) {
        return new Result(destination);
    }

    public static Result errorResult(String message) {
        return new Result(message, -1, null);
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

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public boolean success() {
        return code == 1;
    }
}
