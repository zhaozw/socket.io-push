package com.yy.androidlib.websocket;

import android.util.Log;
import com.google.gson.Gson;

import java.lang.reflect.Type;

public abstract class ReplyHandler<T> {

    private Class clazz;
    private Type type;

    public ReplyHandler() {

    }

    public ReplyHandler(Class clazz) {
        this.clazz = clazz;
    }

    public ReplyHandler(Type type) {
        this.type = type;
    }

    public abstract void onSuccess(T result);

    public abstract void onError(int code, String message);

    public T handle(Gson gson, String body, int respCode, String msg, String serverDataParseErrorTips) {
        T json = null;
        try {

            if (respCode == 1) {
                if (!StringUtils.isEmpty(body)) {
                    if (clazz != null) {
                        json = (T) gson.fromJson(body, clazz);
                    } else if (type != null) {
                        json = gson.fromJson(body, type);
                    } else {
                        json = (T) "success";
                    }
                }
                this.onSuccess(json);
            } else {
                this.onError(respCode, msg);
            }
        } catch (Exception exception) {
            Log.e("STOMP", "parse json error!", exception);
            this.onError(-1, serverDataParseErrorTips);
        }
        return json;
    }
}
