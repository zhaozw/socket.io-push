package com.yy.httpproxy.serializer;

import java.io.UnsupportedEncodingException;

/**
 * Created by xuduo on 10/20/15.
 */
public class StringPushSerializer implements PushSerializer {
    @Override
    public Object toObject(String topic, Object clazz, byte[] body) {
        try {
            return new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
