package com.yy.httpproxy;


public abstract class ReplyHandler<T> {

    public Object clazz;

    public ReplyHandler(Object clazz) {
        this.clazz = clazz;
    }

    public abstract void onSuccess(T result);

    public abstract void onError(int code, String message);

}
