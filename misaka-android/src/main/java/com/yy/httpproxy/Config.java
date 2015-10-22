package com.yy.httpproxy;

import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.serializer.PushSerializer;
import com.yy.httpproxy.serializer.RequestSerializer;
import com.yy.httpproxy.subscribe.PushIdGenerator;
import com.yy.httpproxy.subscribe.PushSubscriber;

/**
 * Created by xuduo on 10/19/15.
 */
public class Config {

    private HttpRequester requester;
    private RequestSerializer requestSerializer;
    private PushSubscriber pushSubscriber;
    private PushIdGenerator pushIdGenerator;
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

    public PushSubscriber getPushSubscriber() {
        return pushSubscriber;
    }

    public Config setPushSubscriber(PushSubscriber pushSubscriber) {
        this.pushSubscriber = pushSubscriber;
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
}
