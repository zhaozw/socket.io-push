package com.yy.httpproxy.requester;

import java.util.Map;

public interface ResponseHandler {

    void onSuccess(Map<String, String> headers, int statusCode, byte[] body);

    void onError(RequestException exception);

}
