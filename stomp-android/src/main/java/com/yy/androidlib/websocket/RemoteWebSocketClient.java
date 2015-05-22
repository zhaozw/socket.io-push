//package com.yy.androidlib.websocket;
//
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.IBinder;
//import android.os.RemoteException;
//import android.util.Log;
//import com.yy.androidlib.websocket.service.IStompRemote;
//import com.yy.androidlib.websocket.service.IWebSocketListener;
//import com.yy.androidlib.websocket.service.StompRemote;
//import com.yy.androidlib.websocket.service.StompService;
//
//public class RemoteWebSocketClient extends IWebSocketListener.Stub implements IWebSocketClient, ServiceConnection {
//
//    private IStompRemote stompRemote;
//    private String url;
//    private WebSocketListener listener;
//
//    public RemoteWebSocketClient(Context context, String url, WebSocketListener listener) {
//        this.url = url;
//        this.listener = listener;
//        startService(context);
//    }
//
//    private void startService(Context context) {
//        Intent intent = new Intent(context, StompService.class);
//        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
//        context.startService(intent);
//    }
//
//    @Override
//    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//        Log.i("STOMP", "onServiceConnected");
//        stompRemote = StompRemote.asInterface(iBinder);
//        connect();
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName componentName) {
//        stompRemote = null;
//        Log.i("STOMP", "onServiceDisconnected");
//    }
//
//    @Override
//    public boolean send(String data) {
//        if (stompRemote == null) {
//            return false;
//        }
//        try {
//            return stompRemote.send(data);
//        } catch (RemoteException e) {
//            Log.e("STOMP", "remote exception", e);
//            return false;
//        }
//    }
//
//    @Override
//    public void connect() {
//        if (stompRemote != null) {
//            try {
//                stompRemote.connect(url, this);
//            } catch (RemoteException e) {
//                Log.e("STOMP", "remote exception", e);
//            }
//        }
//    }
//
//    @Override
//    public void reconnect() {
//
//    }
//
//    @Override
//    public void onConnect() throws RemoteException {
//        listener.onConnect();
//    }
//
//    @Override
//    public void onMessage(String message) throws RemoteException {
//        listener.onMessage(message);
//    }
//
//    @Override
//    public void onDisconnect(String reason) throws RemoteException {
//        listener.onDisconnect(reason,new RuntimeException(reason));
//    }
//
//}
