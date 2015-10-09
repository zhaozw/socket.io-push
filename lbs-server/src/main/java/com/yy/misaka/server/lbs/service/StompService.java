package com.yy.misaka.server.lbs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.*;
import com.yy.misaka.server.lbs.domain.LoginRequest;
import com.yy.misaka.server.lbs.domain.RegisterRequest;
import com.yy.misaka.server.lbs.service.Uaas.RegisterResult;
import com.yy.misaka.server.lbs.service.Uaas.SmsResult;
import com.yy.misaka.server.support.ReplyMessagingTemplate;
import com.yy.misaka.server.support.StompPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;

@Service
public class StompService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ReplyMessagingTemplate messagingTemplate;
//    @Autowired
    private AsyncHttpClient asyncHttpClient;
    @Autowired
    private Uaas uaas;
    @Autowired
    private ObjectMapper mapper;

    public void login(Message<Object> message, StompPrincipal principal, LoginRequest user) {
        user = uaas.login(user.getPhone(), user.getPassword(), user.getAppId());
        messagingTemplate.replyToUserSuccess(message, user);
        messagingTemplate.putUser(user.getUid(), principal.getName());
        principal.setUserId(user.getUid());
        principal.setLoginAppId(user.getAppId());
    }

    public void getRegisterSms(Message<Object> message, RegisterRequest registerRequest) {
        SmsResult smsResult = uaas.getSms(registerRequest.getMobile(), registerRequest.getOperType(), registerRequest.getCountryCode(), registerRequest.getAppId());
        messagingTemplate.replyToUserSuccess(message, smsResult);
    }

    public void register(Message<Object> message, StompPrincipal principal, RegisterRequest registerRequest) {
        RegisterResult registerResult = uaas.register(registerRequest);
        messagingTemplate.replyToUserSuccess(message, registerResult);
        messagingTemplate.putUser(registerResult.getData().getUuid(), principal.getName());
        principal.setUserId(registerResult.getData().getUuid());
        principal.setLoginAppId(registerRequest.getAppId());
    }

    public void request(Map<String, String> headerMap, final StompPrincipal principal, String url, byte[] payload, final boolean dataAsBody, final Message<Object> message) {
        String uid = principal.getUserId();
        String data = "";
        String myIp = System.getProperty("dragon.ip");
        String xForwardedFor = myIp == null ? principal.getRemoteIp() : principal.getRemoteIp() + "," + myIp;
        if (payload != null) {
            try {
                data = new String(payload, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
        }
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(url);
        if (dataAsBody) {
            builder.setHeader("Content-Type", "application/json");
            builder.setBody(payload);
        } else {
            builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addQueryParam("data", data)
                    .addQueryParam("appId", headerMap.get("appId")).addQueryParam("sign", "");
        }

        for (Map.Entry<String, String> headers : principal.headers().entrySet()) {
            builder.setHeader(headers.getKey(), headers.getValue());
        }

        for (Map.Entry<String, String> headers : headerMap.entrySet()) {
            builder.setHeader(headers.getKey(), headers.getValue());
        }

        builder.setHeader("X-Uid", uid)
                .setHeader("X-LoginAppId", principal.getLoginAppId())
                .setHeader("X-Real-IP", principal.getRemoteIp())
                .setHeader("X-Forwarded-For", xForwardedFor);

        builder
                .execute(
                        new AsyncCompletionHandler<Integer>() {

                            @Override
                            public Integer onCompleted(Response response) throws Exception {
                                // Do something with the Response
                                logger.info("onCompleted {} ", response.getResponseBody());
                                String misakaWrite = response.getHeader("misaka-write");
                                if (misakaWrite != null) {
                                    try {
                                        JsonNode jsonNode = mapper.readTree(misakaWrite);
                                        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                                        while (fields.hasNext()) {
                                            Map.Entry<String, JsonNode> entry = fields.next();
                                            String key = entry.getKey();
                                            String value = entry.getValue().asText();
                                            logger.info("misaka write key {} value {} ", key, value);
                                            principal.addHeader(key, value);
                                        }
                                    } catch (Exception exception) {
                                        logger.error("parse write misaka error!");
                                    }
                                }

                                String codeStr = response.getHeader("response-code");
                                String messageStr = response.getHeader("response-message");
                                String decoded;
                                int code;
                                if (codeStr != null && messageStr != null) {
                                    try {
                                        code = Integer.parseInt(codeStr);
                                        decoded = URLDecoder.decode(messageStr, "UTF-8");
                                    } catch (Exception e) {
                                        logger.error("parse int error ", e);
                                        code = -1000;
                                        decoded = "parse server header error!";
                                    }
                                    messagingTemplate.replyToUser(message, null, code, decoded);
                                } else {
                                    if (dataAsBody) {
                                        messagingTemplate.replyToUserSuccess(message, response.getResponseBody());
                                    } else {
                                        try {
                                            JsonNode nyy = mapper.readTree(response.getResponseBody());
                                            JsonNode data = nyy.get("data");
                                            JsonNode nyyCode = nyy.get("code");
                                            if (nyy != null && data != null) {
                                                messagingTemplate.replyToUserSuccess(message, nyy.get("data").toString());
                                            } else if (nyy != null && nyyCode != null && nyyCode.isInt()) {
                                                JsonNode nyyMsg = nyy.get("msg");
                                                String msg;
                                                if (nyyMsg.isTextual()) {
                                                    msg = nyyMsg.textValue();
                                                } else {
                                                    msg = "解析服务器数据错误!";
                                                }
                                                messagingTemplate.replyToUser(message, null, nyyCode.asInt(), msg);
                                            } else {
                                                messagingTemplate.replyToUser(message, null, -10004, "解析服务器数据错误!");
                                            }
                                        } catch (Exception e) {
                                            messagingTemplate.replyToUser(message, null, -10004, "解析服务器数据错误!");
                                        }
                                    }
                                }
                                return 0;
                            }

                            @Override
                            public void onThrowable(Throwable t) {
                                logger.error("onThrowable", t);
                                messagingTemplate.replyToUser(message, null, -11, "应用服务器访问失败");
                            }
                        }

                );
    }
}
