package com.yy.httpproxy.subscribe;

/**
 * Created by xuduo on 10/20/15.
 */
public interface PushSubscriber {

    void setPushIdGenerator(PushIdGenerator pushIdGenerator);

    void subscribeBroadcast(String topic);

    void setPushCallback(PushCallback proxyClient);
}
