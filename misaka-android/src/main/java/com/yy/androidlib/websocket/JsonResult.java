package com.yy.androidlib.websocket;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonResult {

    private Gson gson;
    private String str;
    private Object result;

    public JsonResult(Gson gson, String result) {
        this.gson = gson;
        this.str = result;
    }

    public JsonResult(Object result) {
        this.result = result;
    }

    public static JsonResult errorResult(String message) {
        JsonResult jr = new JsonResult(Result.errorResult(message));
        return jr;
    }

    public <T> T parse(Class<T> clazz) {
        if (result == null) {
            result = gson.fromJson(str, clazz);
        }
        return (T) result;
    }

    public <T> T parse(Type type) {
        if (result == null) {
            result = gson.fromJson(str, type);
        }
        return (T) result;
    }

    public Result simpleResult() {
        if (result == null) {
            result = gson.fromJson(str, Result.class);
        }
        return (Result) result;
    }
}
