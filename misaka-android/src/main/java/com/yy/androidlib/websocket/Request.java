package com.yy.androidlib.websocket;

/**
 * Created by xuduo on 3/18/15.
 */
public class Request {

    private String appId;
    private String destination;
    private Object body;
    private ReplyHandler replyHandler;
    private long timestamp = System.currentTimeMillis();

    public Request(String appId, String destination, Object body, ReplyHandler replyHandler) {
        this.appId = appId;
        this.destination = destination;
        this.body = body;
        this.replyHandler = replyHandler;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public ReplyHandler getReplyHandler() {
        return replyHandler;
    }

    public void setReplyHandler(ReplyHandler replyHandler) {
        this.replyHandler = replyHandler;
    }

    public boolean timeoutForReconnect() {
        return System.currentTimeMillis() - timestamp > 4000;
    }

    public boolean timeoutForRequest() {
        return System.currentTimeMillis() - timestamp > 10000;
    }

    public long getTimestamp(){
        return this.timestamp;
    }
}
