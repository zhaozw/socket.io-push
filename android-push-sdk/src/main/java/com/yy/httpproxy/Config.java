package com.yy.httpproxy;

import android.content.Context;

import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.serializer.PushSerializer;
import com.yy.httpproxy.serializer.RequestSerializer;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.subscribe.ConnectCallback;
import com.yy.httpproxy.subscribe.PushIdGenerator;
import com.yy.httpproxy.subscribe.PushSubscriber;

/**
 * Created by xuduo on 10/19/15.
 */
public class Config {

    private RemoteClient remoteClient;
    private Context context;
    private RequestSerializer requestSerializer;
    private PushIdGenerator pushIdGenerator;
    private PushSerializer pushSerializer;
    private ConnectCallback connectCallback;
    private String host;
    private String notificationHandler;

    public Config(Context context) {
        this.context = context;
    }

    public RemoteClient getRemoteClient() {
        if (remoteClient == null) {
            remoteClient = new RemoteClient(context, host, notificationHandler);
        }
        return remoteClient;
    }

    public Config setHost(String host) {
        this.host = host;
        return this;
    }

    public RequestSerializer getRequestSerializer() {
        return requestSerializer;
    }

    public Config setRequestSerializer(RequestSerializer requestSerializer) {
        this.requestSerializer = requestSerializer;
        return this;
    }

    public PushIdGenerator getPushIdGenerator() {
        return pushIdGenerator;
    }

    public Config setPushIdGenerator(PushIdGenerator pushIdGenerator) {
        this.pushIdGenerator = pushIdGenerator;
        return this;
    }

    public PushSerializer getPushSerializer() {
        return pushSerializer;
    }

    public Config setPushSerializer(PushSerializer pushSerializer) {
        this.pushSerializer = pushSerializer;
        return this;
    }

    public Config setNotificationHandler(String notificationHandler) {
        this.notificationHandler = notificationHandler;
        return this;
    }

    public ConnectCallback getConnectCallback() {
        return connectCallback;
    }

    public Config setConnectCallback(ConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
        return this;
    }
}
