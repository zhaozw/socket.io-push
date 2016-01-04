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

public class ForegroundService extends Service {

    private final String TAG = "FakeService";

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setPriority(Notification.PRIORITY_MIN);
        startForeground(12345, builder.build());
        ConnectionService.beginForeground();
        stopSelf();
        Log.i(TAG, "FakeService onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "FakeService onDestroy");
        stopForeground(true);
    }
}
