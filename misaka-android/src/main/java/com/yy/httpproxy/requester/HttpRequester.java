package com.yy.httpproxy.requester;

/**
 * Created by xuduo on 10/16/15.
 */
public interface HttpRequester {

    void request(RequestInfo requestInfo, ResponseHandler handler);

}
