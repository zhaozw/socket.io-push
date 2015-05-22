package com.yy.misaka.server.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.misaka.server.demo.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

@Service
public class BroadcastService {

    @Autowired
    private ObjectMapper objectMapper;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public void broadcast(String pushId, String uri, Object payload) {
        try {
            HttpUtil.getUrlAsString("http://127.0.0.1:8090/api/broadcast?destination=" + uri + "&pushId=" + pushId + "&data="
                    + URLEncoder.encode(objectMapper.writeValueAsString(payload), "UTF-8"));
        } catch (Exception e) {
            logger.error("broadcast error!", e);
        }
    }

    public void pushToUser(String uid, String pushId, String uri, Object payload) {
        try {
            String result = HttpUtil.getUrlAsString("http://127.0.0.1:8090/api/pushToUser?uid=" + uid + "&destination=" + uri + "&pushId=" + pushId + "&data="
                    + URLEncoder.encode(objectMapper.writeValueAsString(payload), "UTF-8"));
        } catch (Exception e) {
            logger.error("broadcast error!", e);
        }
    }
}
