package com.yy.misaka.support;

/**
 * Created by xuduo on 3/10/15.
 */
public class ResultException extends RuntimeException {

    private Result result;

    public ResultException(int code, String message) {
        result = new Result();
        result.setCode(code);
        result.setMessage(message);
    }

    public Result getResult() {
        return result;
    }
}
