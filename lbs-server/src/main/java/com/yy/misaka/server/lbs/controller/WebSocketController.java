package com.yy.misaka.server.lbs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.misaka.server.lbs.domain.LoginRequest;
import com.yy.misaka.server.lbs.domain.RegisterRequest;
import com.yy.misaka.server.lbs.service.StompService;
import com.yy.misaka.server.support.BaseMessageController;
import com.yy.misaka.server.support.StompPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class WebSocketController extends BaseMessageController {

    @Autowired
    private WebSocketMessageBrokerStats webSocketMessageBrokerStats;
    @Autowired
    private StompService stompService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/request")
    public void onMessage(Message<Object> message, @Header String url, @Header String appId, @Header(required = false, defaultValue = "false") boolean dataAsBody, @Payload(required = false) byte[] payload, StompPrincipal principal) throws Exception {
        String payloadText;
        if (payload == null) {
            payloadText = "";
        } else {
            payloadText = new String(payload);
        }
        logger.info("onMessage url {} payload {} appId {} dataAsBody {}", url, payloadText, appId, dataAsBody);
        if ("login".equals(appId)) {
            URL u = new URL(url);
            if ("/login".equals(u.getPath())) {
                LoginRequest user = objectMapper.readValue(payload, LoginRequest.class);
                stompService.login(message, principal, user);
            } else if ("/getRegisterSms".equals(u.getPath())) {
                RegisterRequest registerRequest = objectMapper.readValue(payload, RegisterRequest.class);
                stompService.getRegisterSms(message, registerRequest);
            } else if ("/register".equals(u.getPath())) {
                RegisterRequest registerRequest = objectMapper.readValue(payload, RegisterRequest.class);
                stompService.register(message, principal, registerRequest);
            }
        } else {
            stompService.request(appId, principal, url, payload, dataAsBody,message);
        }
    }

    private static AtomicLong count = new AtomicLong(0L);
    private static volatile long st = System.currentTimeMillis();
    private static final long INTERVAL = 10000;

    @MessageMapping("/testRequest")
    public void test(Message<Object> message, @Payload(required = false) byte[] payload, StompPrincipal principal) {
        long c = count.getAndIncrement();
        messagingTemplate.replyToUserSuccess(message, "Hello World!");
        long et = System.currentTimeMillis();
        if (c % INTERVAL == INTERVAL - 1 || (et - st >= 1000)) {
            long sts = st;
            st = et;
            logger.info("======At The " + c + " Req Cost: " + (et - sts) + "ms");
        }
    }

    @SubscribeMapping("/user/queue/reply")
    public String onSubscribeReply(Message<Object> message, StompPrincipal principal) throws Exception {
        logger.info("onSubscribeReply " + principal.getRemoteIp());
        return "CONNECTED";
    }

//    @RequestMapping(value = "/api/requests")
//    public
//    @ResponseBody
//    int requests() {
//        long old = System.currentTimeMillis() - 120 * 1000;
//        int count = 0;
//        for (long time : request.values()) {
//            if (time > old) {
//                count++;
//            }
//        }
//        return count / 120;
//    }

    @RequestMapping(value = "/api/stats")
    public
    @ResponseBody
    WebSocketMessageBrokerStats stats() {
        return webSocketMessageBrokerStats;
    }


}