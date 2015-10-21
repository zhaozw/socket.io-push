package com.yy.httpproxy.serializer;

public interface PushSerializer {

    Object toObject(String topic, Object clazz, byte[] body);
}
