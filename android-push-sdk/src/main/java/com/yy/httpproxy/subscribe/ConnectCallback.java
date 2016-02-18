package com.yy.httpproxy.subscribe;

/**
 * Created by xuduo on 10/20/15.
 */
public interface ConnectCallback {

    void onConnect(String uid);

    void onDisconnect();

}
