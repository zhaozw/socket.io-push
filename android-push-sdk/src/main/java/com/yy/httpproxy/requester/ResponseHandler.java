package com.yy.httpproxy.requester;


public interface ResponseHandler {

    void onResponse(String sequenceId, int code, String message, byte[] body);

}
