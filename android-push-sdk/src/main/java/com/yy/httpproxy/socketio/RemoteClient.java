package com.yy.httpproxy.socketio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.yy.httpproxy.ProxyClient;
import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.service.RemoteService;
import com.yy.httpproxy.subscribe.PushSubscriber;

import java.util.HashSet;
import java.util.Set;


public class RemoteClient implements PushSubscriber, HttpRequester {

    private static final String TAG = "RemoteClient";
    public static final int CMD_SUBSCRIBE_BROADCAST = 1;
    public static final int CMD_SET_PUSH_ID = 2;
    public static final int CMD_REQUEST = 3;
    public static final int CMD_REGISTER_CLIENT = 4;
    public static final int CMD_UNSUBSCRIBE_BROADCAST = 5;
    private Set<String> topics = new HashSet<>();
    private ProxyClient proxyClient;
    private Messenger mService;
    private boolean mBound;
    private String pushId;

    private final Messenger messenger = new Messenger(new IncomingHandler());

    public void unsubscribeBroadcast(String topic) {
        Message msg = Message.obtain(null, CMD_UNSUBSCRIBE_BROADCAST, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("topic", topic);
        msg.setData(bundle);
        sendMsg(msg);
        topics.remove(topic);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int cmd = msg.what;
            Bundle bundle = msg.getData();
            if (cmd == RemoteService.CMD_RESPONSE) {
                String message = bundle.getString("message");
                int code = bundle.getInt("code", 1);
                byte[] data = bundle.getByteArray("data");
                int sequenceId = bundle.getInt("sequenceId", 1);
                proxyClient.onResponse("",sequenceId, code, message, data);
            } else if (cmd == RemoteService.CMD_PUSH) {
                String topic = bundle.getString("topic");
                byte[] data = bundle.getByteArray("data");
                proxyClient.onPush(topic, data);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            Log.i(TAG, "onServiceConnected");
            mService = new Messenger(service);
            mBound = true;

            Message msg = Message.obtain(null, CMD_REGISTER_CLIENT, 0, 0);
            msg.replyTo = messenger;
            sendMsg(msg);

            for (String topic : topics) {
                doSubscribe(topic);
            }

            setPushId(pushId);
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    public RemoteClient(Context context, String host, String notificationHandler) {
        Intent intent = new Intent(context, RemoteService.class);
        intent.putExtra("host", host);
        if (notificationHandler != null) {
            intent.putExtra("notificationHandler", notificationHandler);
        }
//        context.startService(intent);
        context.bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void sendMsg(Message msg) {
        try {
            if (mBound) {
                mService.send(msg);
            }
        } catch (Exception e) {
            Log.e(TAG, "sendMsg error!", e);
        }
    }

    public void setPushId(String pushId) {
        Message msg = Message.obtain(null, CMD_SET_PUSH_ID, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("pushId", pushId);
        msg.setData(bundle);
        sendMsg(msg);
        this.pushId = pushId;
    }

    public void request(RequestInfo requestInfo) {
        Message msg = Message.obtain(null, CMD_REQUEST, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("requestInfo", requestInfo);
        msg.setData(bundle);
        sendMsg(msg);
    }

    @Override
    public void subscribeBroadcast(String topic) {
        doSubscribe(topic);
    }

    private void doSubscribe(String topic) {
        Message msg = Message.obtain(null, CMD_SUBSCRIBE_BROADCAST, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("topic", topic);
        msg.setData(bundle);
        sendMsg(msg);
        topics.add(topic);
    }

    public void setProxyClient(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }
}
