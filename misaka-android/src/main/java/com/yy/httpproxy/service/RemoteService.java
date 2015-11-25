package com.yy.httpproxy.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.serializer.StringPushSerializer;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.socketio.SocketIOProxyClient;
import com.yy.httpproxy.subscribe.PushCallback;
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import org.json.JSONObject;

import java.util.Map;

public class RemoteService extends Service implements PushCallback, ResponseHandler, SocketIOProxyClient.NotificationCallback {

    private static final String INTENT_TAIL = ".YY_REMOTE_SERVICE";
    public static final int CMD_CREATED = 1;
    public static final int CMD_PUSH = 2;
    public static final int CMD_NOTIFICATION_CLICKED = 3;
    public static final int CMD_RESPONSE = 4;
    private final String TAG = "SocketIoService";
    private SocketIOProxyClient client;
    private NotificationHandler notificationHandler;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cmd = intent.getIntExtra("cmd", 0);
            Log.v(TAG, "RemoteService onReceive " + cmd);
            if (cmd == RemoteClient.CMD_SUBSCRIBE_BROADCAST) {
                String topic = intent.getStringExtra("topic");
                client.subscribeBroadcast(topic);
            } else if (cmd == RemoteClient.CMD_SET_PUSH_ID) {
                client.setPushId(intent.getStringExtra("pushId"));
            } else if (cmd == RemoteClient.CMD_REQUEST) {
                RequestInfo info = (RequestInfo) intent.getSerializableExtra("requestInfo");
                client.request(info);
            }
        }
    };

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
                    Log.e(TAG, "handlerClass error");
                    notificationHandler = new DefaultNotificationHandler();
                }
            }

            com.yy.httpproxy.Config config = new com.yy.httpproxy.Config();
            client = new SocketIOProxyClient(host);
            client.setResponseHandler(this);
            client.setPushId(new SharedPreferencePushIdGenerator(this.getApplicationContext()).generatePushId());
            config.setPushSubscriber(client);
            config.setPushSerializer(new StringPushSerializer());
            client.setPushCallback(this);
            client.setNotificationCallback(this);
            registerReceiver(broadcastReceiver, new IntentFilter(RemoteClient.getIntentName(this)));

        }
        sendCreated();
        return Service.START_STICKY;
    }

    private void sendCreated() {
        Intent intent = new Intent(getIntentName(this));
        intent.putExtra("cmd", CMD_CREATED);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPush(String topic, byte[] data) {
        Log.d(TAG, "push recived " + topic);
        Intent intent = new Intent(getIntentName(this));
        intent.putExtra("cmd", CMD_PUSH);
        intent.putExtra("topic", topic);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }

    public static String getIntentName(Context context) {
        return context.getPackageName() + INTENT_TAIL;
    }


    @Override
    public void onNotification(String id, JSONObject data) {
        PushedNotification notification = new PushedNotification(id, data);
        notificationHandler.handlerNotification(this, notification);
    }

    @Override
    public void onResponse(int sequenceId, int code, String message, byte[] data) {
        Log.d(TAG, "onResponse  " + code);
        Intent intent = new Intent(getIntentName(this));
        intent.putExtra("cmd", CMD_RESPONSE);
        intent.putExtra("sequenceId", sequenceId);
        intent.putExtra("code", code);
        intent.putExtra("message", message);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }

}
