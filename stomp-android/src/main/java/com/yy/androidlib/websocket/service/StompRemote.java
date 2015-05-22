//package com.yy.androidlib.websocket.service;
//
//import android.os.RemoteException;
//import android.util.Log;
//import com.yy.androidlib.websocket.IWebSocketClient;
//import com.yy.androidlib.websocket.WebSocketFactory;
//import com.yy.androidlib.websocket.WebSocketListener;
//
//public class StompRemote extends IStompRemote.Stub implements WebSocketListener {
//
//    private IWebSocketClient localWebSocketClient;
//    private IWebSocketListener listener;
//
//    public boolean send(String data) {
//        Log.i("STOMP", "StompRemote.send called " + data);
//        localWebSocketClient.send(data);
//        return true;
//    }
//
//    public void connect(String url, IWebSocketListener listener) {
//        this.listener = listener;
//        Log.i("STOMP", "StompRemote connect url " + url + " ,listener:" + listener);
//        localWebSocketClient = WebSocketFactory.local(url, this);
//        localWebSocketClient.connect();
//    }
//
//    @Override
//    public void onConnect() {
//        try {
//            listener.onConnect();
//        } catch (RemoteException e) {
//            Log.e("STOMP", "RemoteException", e);
//        }
//    }
//
//    @Override
//    public void onMessage(String message) {
//        try {
//            listener.onMessage(message);
//        } catch (RemoteException e) {
//            Log.e("STOMP", "RemoteException", e);
//        }
//    }
//
//    @Override
//    public void onDisconnect(String reason, Exception e) {
//        try {
//            listener.onDisconnect(reason);
//        } catch (RemoteException ex) {
//            Log.e("STOMP", "RemoteException", ex);
//        }
//    }
//
//    @Override
//    public void onHeartbeat() {
//
//    }
//
//
//}
