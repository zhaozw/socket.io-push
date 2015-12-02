package com.yy.httpproxy.emitter;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class JsonSerializer implements Serializer {

    private Gson gson = new Gson();


    @Override
    public Object toObject(String path, Object clazz, byte[] body) throws Exception {
        if (clazz instanceof Class) {
            return gson.fromJson(new String(body, "UTF-8"), (Class) clazz);
        } else {
            return gson.fromJson(new String(body, "UTF-8"), (Type) clazz);
        }
    }

    @Override
    public byte[] toBinary(String path, Object body) {
        String data = gson.toJson(body);
        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}

