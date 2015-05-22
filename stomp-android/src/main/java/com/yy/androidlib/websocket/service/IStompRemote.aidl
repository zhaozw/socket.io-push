package com.yy.androidlib.websocket.service;

import com.yy.androidlib.websocket.service.IWebSocketListener;

interface IStompRemote {

    boolean send(String data);

    void connect(String url, IWebSocketListener listener);

}
