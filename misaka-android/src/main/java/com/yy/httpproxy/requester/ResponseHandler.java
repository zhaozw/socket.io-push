package com.yy.httpproxy.requester;


public interface ResponseHandler {

    void onResponse(int sequenceId, int code, String message, byte[] body);

}
