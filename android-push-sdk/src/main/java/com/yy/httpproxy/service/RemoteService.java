package com.yy.httpproxy.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.serializer.StringPushSerializer;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.socketio.SocketIOProxyClient;
import com.yy.httpproxy.subscribe.PushCallback;
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import org.json.JSONObject;

public class RemoteService extends Service implements PushCallback, ResponseHandler, SocketIOProxyClient.NotificationCallback {

    public static final int CMD_PUSH = 2;
    public static final int CMD_NOTIFICATION_CLICKED = 3;
    public static final int CMD_RESPONSE = 4;
    private final String TAG = "SocketIoService";
    private SocketIOProxyClient client;
    private NotificationHandler notificationHandler;
    private final Messenger messenger = new Messenger(new IncomingHandler());
    private Messenger remoteClient;

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int cmd = msg.what;
            Bundle bundle = msg.getData();
            if (cmd == RemoteClient.CMD_SUBSCRIBE_BROADCAST) {
                String topic = bundle.getString("topic");
                client.subscribeBroadcast(topic);
            } else if (cmd == RemoteClient.CMD_SET_PUSH_ID) {
                client.setPushId(bundle.getString("pushId"));
            } else if (cmd == RemoteClient.CMD_REQUEST) {
                RequestInfo info = (RequestInfo) bundle.getSerializable("requestInfo");
                client.request(info);
            } else if (cmd == RemoteClient.CMD_REGISTER_CLIENT) {
                remoteClient = msg.replyTo;
            } else if (cmd == RemoteClient.CMD_UNSUBSCRIBE_BROADCAST) {
                String topic = bundle.getString("topic");
                client.unsubscribeBroadcast(topic);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
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
        Log.d(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
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
        return messenger.getBinder();
    }

    @Override
    public void onPush(String topic, byte[] data) {
        Log.d(TAG, "push recived " + topic);

        Message msg = Message.obtain(null, CMD_PUSH, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("topic", topic);
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        sendMsg(msg);
    }

    @Override
    public void onNotification(String id, JSONObject data) {
        PushedNotification notification = new PushedNotification(id, data);
        notificationHandler.handlerNotification(this, notification);
    }

    @Override
    public void onResponse(String sequenceId, int code, String message, byte[] data) {
        Log.d(TAG, "onResponse  " + code);

        Message msg = Message.obtain(null, CMD_RESPONSE, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("sequenceId", sequenceId);
        bundle.putInt("code", code);
        bundle.putString("message", message);
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        sendMsg(msg);
    }

    private void sendMsg(Message msg) {
        try {
            remoteClient.send(msg);
        } catch (Exception e) {
            Log.e(TAG, "sendMsg error!", e);
        }
    }


}
