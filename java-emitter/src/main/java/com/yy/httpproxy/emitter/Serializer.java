package com.yy.httpproxy.emitter;

public interface Serializer {

    Object toObject(String path, Object clazz, byte[] body);

}
