package com.yy.httpproxy.serializer;


public interface RequestSerializer extends PushSerializer {

    byte[] toBinary(String path, Object body);


}
