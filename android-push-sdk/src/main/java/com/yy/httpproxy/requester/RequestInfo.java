package com.yy.httpproxy.requester;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xuduo on 10/16/15.
 */
public class RequestInfo implements Serializable{

    private String host;
    private int port;
    private int sequenceId;
    private String path;
    private Map<String, String> headers;
    private byte[] body;
    private String method;
    private String scheme;
    private boolean expectReply = false;
    private long timestamp = System.currentTimeMillis();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getUrl(){
        return  scheme + "://" + getHost() + ":" + getPort() + getPath();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean timeoutForRequest(long timeout) {
        return System.currentTimeMillis() - timestamp > timeout;
    }

    public boolean isExpectReply() {
        return expectReply;
    }

    public void setExpectReply(boolean expectReply) {
        this.expectReply = expectReply;
    }
}
