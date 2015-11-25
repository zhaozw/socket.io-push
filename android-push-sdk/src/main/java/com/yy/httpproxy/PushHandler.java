package com.yy.httpproxy;

/**
 * Created by xuduo on 10/20/15.
 */
public abstract class PushHandler<T> extends ReplyHandler<T> {

    public PushHandler(Object clazz) {
        super(clazz);
    }

    @Override
    public void onError(int code, String message) {

    }
}
