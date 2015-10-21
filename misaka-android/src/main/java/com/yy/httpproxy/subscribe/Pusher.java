package com.yy.httpproxy.subscribe;

/**
 * Created by xuduo on 10/20/15.
 */
public interface Pusher {

    void setPushGenerator(PushGenerator pushGenerator);

    void subscribe(String topic);

    void setPushCallback(PushCallback proxyClient);
}
