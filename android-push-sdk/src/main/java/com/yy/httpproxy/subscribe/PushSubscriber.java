package com.yy.httpproxy.subscribe;

/**
 * Created by xuduo on 10/20/15.
 */
public interface PushSubscriber {

    void subscribeBroadcast(String topic, boolean receiveTtlPackets);

}
