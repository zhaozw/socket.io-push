package com.yy.httpproxy.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.socketio.RemoteClient;

public class BindService extends Service {

    public static final int CMD_PUSH = 2;
    public static final int CMD_NOTIFICATION_CLICKED = 3;
    public static final int CMD_NOTIFICATION_ARRIVED = 5;
    public static final int CMD_RESPONSE = 4;
    public static final int CMD_CONNECTED = 5;
    public static final int CMD_DISCONNECT = 6;
    private static final String TAG = "BindService";
    private final Messenger messenger = new Messenger(new IncomingHandler());
    public static Messenger remoteClient;
    public static boolean bound = false;

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int cmd = msg.what;
            Bundle bundle = msg.getData();
            if (cmd == RemoteClient.CMD_SUBSCRIBE_BROADCAST) {
                String topic = bundle.getString("topic");
                ConnectionService.client.subscribeBroadcast(topic);
            } else if (cmd == RemoteClient.CMD_SET_PUSH_ID) {
                ConnectionService.client.setPushId(bundle.getString("pushId"));
            } else if (cmd == RemoteClient.CMD_REQUEST) {
                RequestInfo info = (RequestInfo) bundle.getSerializable("requestInfo");
                ConnectionService.client.request(info);
            } else if (cmd == RemoteClient.CMD_REGISTER_CLIENT) {
                remoteClient = msg.replyTo;
                bound = true;
                ConnectionService.sendConnect();
            } else if (cmd == RemoteClient.CMD_UNSUBSCRIBE_BROADCAST) {
                String topic = bundle.getString("topic");
                ConnectionService.client.unsubscribeBroadcast(topic);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "RemoteService onCreate");
        // startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = "null";
        if (intent != null) {
            host = intent.getStringExtra("host");
        }
        Log.d(TAG, "onStartCommand " + host);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        bound = false;
        return true;
    }

    public static void sendMsg(Message msg) {
        if (bound) {
            try {
                BindService.remoteClient.send(msg);
            } catch (Exception e) {
                Log.e(TAG, "sendMsg error!", e);
            }
        } else {
            Log.v(TAG, "sendMsg not bound");
        }
    }
}
