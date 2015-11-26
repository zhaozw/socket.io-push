package com.yy.httpproxy.serializer;

import com.yy.httpproxy.requester.RequestException;

public interface PushSerializer {

    Object toObject(String topic, Object clazz, byte[] body) throws RequestException;
}
