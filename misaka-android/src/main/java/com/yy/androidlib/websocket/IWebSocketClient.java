package com.yy.androidlib.websocket;

public interface IWebSocketClient {

    boolean send(String data);

    void connect();

    boolean isDisconnected();

    void close();
}
