package com.yy.httpproxy.nyy;


import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;


import org.apache.http.Header;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by xuduo on 10/16/15.
 */
public class LocalHttpRequester implements HttpRequester {

    private AsyncHttpClient client = new AsyncHttpClient();

    public LocalHttpRequester() {
        client = new AsyncHttpClient();
        client.setTimeout(15000);
    }

    @Override
    public void request(final RequestInfo requestInfo, final ResponseHandler handler) {
        Header[] headers;
        if (requestInfo.getHeaders() != null) {
            headers = new Header[requestInfo.getHeaders().size()];
            int i = 0;
            for (Map.Entry<String, String> entry : requestInfo.getHeaders().entrySet()) {
                headers[i] = new BasicHeader(entry.getKey(), entry.getValue());
                i++;
            }
        } else {
            headers = new Header[]{};
        }

        byte[] body = requestInfo.getBody();
        client.post(null, requestInfo.getUrl(), headers, new InputStreamEntity(new ByteArrayInputStream(body), body.length), "application/x-www-form-urlencoded",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Map<String, String> headerMap = new HashMap<String, String>();
                        for (Header header : headers) {
                            headerMap.put(header.getName(), header.getValue());
                        }
                        handler.onSuccess(headerMap, statusCode, responseBody);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("LocalHttpRequester", "onFailure, code: %d" + statusCode, error);
                        handler.onError(new RequestException(error, RequestException.Error.CONNECT_ERROR));
                    }
                });


    }
}
