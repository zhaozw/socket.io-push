package com.yy.httpproxy;

import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.serializer.PushSerializer;
import com.yy.httpproxy.serializer.RequestSerializer;
import com.yy.httpproxy.subscribe.PushGenerator;
import com.yy.httpproxy.subscribe.Pusher;

/**
 * Created by xuduo on 10/19/15.
 */
public class Config {

    private HttpRequester requester;
    private RequestSerializer requestSerializer;
    private Pusher pusher;
    private PushGenerator pushGenerator;
    private PushSerializer pushSerializer;


    public HttpRequester getRequester() {
        return requester;
    }

    public Config setRequester(HttpRequester requester) {
        this.requester = requester;
        return this;
    }

    public RequestSerializer getRequestSerializer() {
        return requestSerializer;
    }

    public Config setRequestSerializer(RequestSerializer requestSerializer) {
        this.requestSerializer = requestSerializer;
        return this;
    }

    public Pusher getPusher() {
        return pusher;
    }

    public Config setPusher(Pusher pusher) {
        this.pusher = pusher;
        return this;
    }

    public PushGenerator getPushGenerator() {
        return pushGenerator;
    }

    public Config setPushGenerator(PushGenerator pushGenerator) {
        this.pushGenerator = pushGenerator;
        return this;
    }

    public PushSerializer getPushSerializer() {
        return pushSerializer;
    }

    public Config setPushSerializer(PushSerializer pushSerializer) {
        this.pushSerializer = pushSerializer;
        return this;
    }
}
