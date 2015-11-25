package com.yy.httpproxy.requester;

import com.yy.httpproxy.ProxyClient;
import com.yy.httpproxy.ReplyHandler;

/**
 * Created by xuduo on 10/16/15.
 */
public interface HttpRequester {

    void request(RequestInfo requestInfo);

    void setProxyClient(ProxyClient proxyClient);
}
