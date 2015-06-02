package com.yy.misaka.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {

    private String host = "http://127.0.0.1:8080";
    @Autowired
    private AsyncHttpClient asyncHttpClient;
    @Autowired
    private ObjectMapper objectMapper;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void broadcast(String pushId, String uri, Object payload) {
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.prepareGet(host + "/api/broadcast");
        builder.addQueryParam("destination", uri);
        builder.addQueryParam("pushId", pushId);
        try {
            builder.addQueryParam("data", objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException", e);
            return;
        }
        asyncHttpClient.executeRequest(builder.build(), new AsyncCompletionHandler<Integer>() {
            @Override
            public Integer onCompleted(Response response) throws Exception {
                logger.debug("broadcast result {}", response.getResponseBody());
                return 0;
            }
        });
    }

    public void pushToUser(String uid, String pushId, String uri, Object payload) {
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.prepareGet(host + "/api/broadcast");
        builder.addQueryParam("destination", uri);
        builder.addQueryParam("pushId", pushId);
        builder.addQueryParam("uid", uid);
        try {
            builder.addQueryParam("data", objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException", e);
            return;
        }
        asyncHttpClient.executeRequest(builder.build());
    }

    public void setHost(String host) {
        this.host = host;
    }
}
