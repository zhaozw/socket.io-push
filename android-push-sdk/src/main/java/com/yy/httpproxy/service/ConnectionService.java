package com.yy.httpproxy.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.socketio.SocketIOProxyClient;
import com.yy.httpproxy.subscribe.PushCallback;
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import org.json.JSONObject;

public class ConnectionService extends Service implements PushCallback, ResponseHandler, SocketIOProxyClient.NotificationCallback {

    private final String TAG = "ConnectionService";
    public static SocketIOProxyClient client;
    private NotificationHandler notificationHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "ConnectionService onCreate");
        // startForeground();
    }

    private void startForeground() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setPriority(Notification.PRIORITY_MIN);
        startForeground(12345, builder.build());
        Intent intent = new Intent(this, ForegroundService.class);
        startService(intent);
    }


    private String getFromIntentOrPref(Intent intent, String name) {
        String value = null;
        if (intent != null) {
            value = intent.getStringExtra(name);
        }
        SharedPreferences pref = getSharedPreferences("RemoteService", MODE_PRIVATE);
        if (value == null) {
            value = pref.getString(name, null);
        } else {
            pref.edit().putString(name, value).commit();
        }
        return value;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = "null";
        if (intent != null) {
            host = intent.getStringExtra("host");
        }
        Log.d(TAG, "onStartCommand " + host);
        initClient(intent);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    private void initClient(Intent intent) {
        if (client == null) {
            String host = getFromIntentOrPref(intent, "host");
            String handlerClassName = getFromIntentOrPref(intent, "notificationHandler");
            Class handlerClass;

            if (handlerClassName == null) {
                notificationHandler = new DefaultNotificationHandler();
            } else {
                try {
                    handlerClass = Class.forName(handlerClassName);
                    notificationHandler = (NotificationHandler) handlerClass.newInstance();
                } catch (Exception e) {
                    Log.e(TAG, "handlerClass error", e);
                    notificationHandler = new DefaultNotificationHandler();
                }
            }

            client = new SocketIOProxyClient(host);
            client.setResponseHandler(this);
            client.setPushId(new SharedPreferencePushIdGenerator(this.getApplicationContext()).generatePushId());
            client.setPushCallback(this);
            client.setNotificationCallback(this);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onPush(String topic, byte[] data) {
        Log.d(TAG, "push recived " + topic);
        Message msg = Message.obtain(null, BindService.CMD_PUSH, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("topic", topic);
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        sendMsg(msg);
    }

    @Override
    public void onNotification(String id, JSONObject data) {
        PushedNotification notification = new PushedNotification(id, data);
        notificationHandler.handlerNotification(this, BindService.bound, notification);
    }

    @Override
    public void onResponse(String sequenceId, int code, String message, byte[] data) {
        Log.d(TAG, "onResponse  " + code);
        Message msg = Message.obtain(null, BindService.CMD_RESPONSE, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("sequenceId", sequenceId);
        bundle.putInt("code", code);
        bundle.putString("message", message);
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        sendMsg(msg);
    }

    private void sendMsg(Message msg) {
        if (BindService.bound) {
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
