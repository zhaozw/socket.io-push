package com.yy.androidlib.websocket.service;

interface IWebSocketListener {

     void onConnect();

     void onMessage(String message);

     void onDisconnect(String reason);

}
