package com.yy.httpproxy.emitter;

public interface Serializer {

    Object toObject(String path, Object clazz, byte[] body) throws Exception;

    byte[] toBinary(String path, Object body) throws Exception;
}
