package com.yy.httpproxy.emitter;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class ByteArraySerializer implements Serializer {

    private Gson gson = new Gson();


    @Override
    public Object toObject(String path, Object clazz, byte[] body) throws Exception {
        return body;
    }

    @Override
    public byte[] toBinary(String path, Object body) {
        return (byte[]) body;
    }
}

