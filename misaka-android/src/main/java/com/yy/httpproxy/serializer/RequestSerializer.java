package com.yy.httpproxy.serializer;

import com.yy.httpproxy.requester.RequestException;

import java.util.Map;

public interface RequestSerializer {

    byte[] toBinary(String path, Object body);

    Object toObject(Object clazz, byte[] body) throws RequestException;

}
