package com.yy.httpproxy.subscribe;

/**
 * Created by xuduo on 10/20/15.
 */
public interface PushCallback {

    void onPush(String topic, byte[] data);

}
