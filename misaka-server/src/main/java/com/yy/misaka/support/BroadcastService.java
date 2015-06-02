package com.yy.misaka.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BroadcastService {

    @Autowired
    private AsyncHttpClient asyncHttpClient;
    @Autowired
    private ObjectMapper objectMapper;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void broadcast(String pushId, String uri, Object payload) {
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost("http://127.0.0.1:8090/api/broadcast");
        builder.addQueryParam("destination", uri);
        builder.addQueryParam("pushId", pushId);
        try {
            builder.addQueryParam("data", objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException", e);
            return;
        }
        asyncHttpClient.executeRequest(builder.build());
    }

    public void pushToUser(String uid, String pushId, String uri, Object payload) {
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost("http://127.0.0.1:8090/api/broadcast");
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
}
