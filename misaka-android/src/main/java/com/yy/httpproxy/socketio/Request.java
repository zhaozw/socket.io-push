package com.yy.httpproxy.socketio;

import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;

/**
 * Created by xuduo on 3/18/15.
 */
public class Request {

    private RequestInfo requestInfo;
    private ResponseHandler responseHandler;
    private long timestamp = System.currentTimeMillis();

    public Request(RequestInfo requestInfo, ResponseHandler responseHandler) {
        this.requestInfo = requestInfo;
        this.responseHandler = responseHandler;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public boolean timeoutForRequest(long timeout) {
        return System.currentTimeMillis() - timestamp > timeout;
    }

    public long getTimestamp(){
        return this.timestamp;
    }

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
