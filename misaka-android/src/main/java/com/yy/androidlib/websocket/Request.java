package com.yy.androidlib.websocket;

/**
 * Created by xuduo on 3/18/15.
 */
public class Request {

    private String url;
    private Object body;
    private ReplyHandler replyHandler;
    private long timestamp = System.currentTimeMillis();

    public Request(String url, Object body, ReplyHandler replyHandler) {
        this.url = url;
        this.body = body;
        this.replyHandler = replyHandler;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
