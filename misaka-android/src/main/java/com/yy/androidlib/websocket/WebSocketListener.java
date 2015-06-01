package com.yy.androidlib.websocket;

public interface WebSocketListener {

    public void onConnect();

    public void onMessage(String message);

    public void onDisconnect(String reason, Exception e);

    void onHeartbeat();
}
