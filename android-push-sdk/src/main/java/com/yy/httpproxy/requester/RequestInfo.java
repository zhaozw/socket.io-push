package com.yy.httpproxy.requester;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Created by xuduo on 10/16/15.
 */
public class RequestInfo implements Serializable{

    private String sequenceId = new BigInteger(130, new SecureRandom()).toString(32);
    private byte[] body;
    private String path;
    private boolean expectReply = false;
    private long timestamp = System.currentTimeMillis();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSequenceId() {
        return sequenceId;
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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isExpectReply() {
        return expectReply;
    }

    public void setExpectReply(boolean expectReply) {
        this.expectReply = expectReply;
    }
}
