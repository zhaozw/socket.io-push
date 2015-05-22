package com.yy.misaka.server.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.*;

@Service
public class ReplyMessagingTemplate {

    private String replayQueue = "/queue/reply";
    private Map<String, String> uidMap = new HashMap<String, String>();

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public void replyToUser(Message<Object> message, Object payload, int code, String msg) {
        Map<String, Object> headers = new HashMap<String, Object>();
        Map sendHeaders = new HashMap();
        sendHeaders.put("nativeHeaders", headers);

        Map nativeHeaders = (Map) message.getHeaders().get("nativeHeaders");
        List requestId = (List) nativeHeaders.get("request-id");
        if (requestId == null) {
            requestId = new ArrayList<String>();
            requestId.add("0");
        }
        headers.put("request-id", requestId);
        headers.put("response-code", Arrays.asList(new String[]{String.valueOf(code)}));
        headers.put("response-message", Arrays.asList(new String[]{msg}));

        if (payload == null) {
            payload = "";
        }
        Principal user = (Principal) message.getHeaders().get("simpUser");
        messagingTemplate.convertAndSendToUser(user.getName(), replayQueue, payload, sendHeaders);
    }

    public void replyToUserRaw(Message<Object> message, Object payload, int code, String msg) {
        Map<String, Object> headers = new HashMap<String, Object>();
        Map sendHeaders = new HashMap();
        sendHeaders.put("nativeHeaders", headers);

        Map nativeHeaders = (Map) message.getHeaders().get("nativeHeaders");
        headers.put("request-id", nativeHeaders.get("request-id"));
        headers.put("response-code", Arrays.asList(new String[]{String.valueOf(code)}));
        headers.put("response-message", Arrays.asList(new String[]{msg}));

        if (payload == null) {
            payload = "";
        }
        Principal user = (Principal) message.getHeaders().get("simpUser");

        String userName = StringUtils.replace(user.getName(), "/", "%2F");
        String userDest = "/user/" + user + replayQueue;

        MessageHeaders messageHeaders = new MessageHeaders(sendHeaders);

        Message<?> toSend = toMessage(payload, messageHeaders);
        if (message == null) {
            String payloadType = (payload != null ? payload.getClass().getName() : null);
            Object contentType = (messageHeaders != null ? messageHeaders.get(MessageHeaders.CONTENT_TYPE) : null);
            throw new MessageConversionException("Unable to convert payload with type='" + payloadType +
                    "', contentType='" + contentType + "', converter=[" + "]");
        }
        messagingTemplate.send(userDest, toSend);

    }

    public Message<?> toMessage(Object payload, MessageHeaders headers) {
        return (payload != null ? MessageBuilder.withPayload(payload).copyHeaders(headers).build() : null);
    }

    public void replyToUserSuccess(Message<Object> message) {
        replyToUserSuccess(message, null);
    }

    public void replyToUserSuccess(Message<Object> message, Object payload) {
        replyToUser(message, payload, 1, "success");
    }

    public void broadcast(String destination, Object payload) {
        Map<String, Object> headers = new HashMap<String, Object>();
        Map sendHeaders = new HashMap();
        sendHeaders.put("nativeHeaders", headers);

        headers.put("response-code", Arrays.asList(new String[]{String.valueOf(1)}));
        headers.put("response-message", Arrays.asList(new String[]{"success"}));

        messagingTemplate.convertAndSend(destination, payload, sendHeaders);
    }

    public void sendToUser(String uid, String destination, Object payload) {
        String user = uidMap.get(uid);
        if (user != null) {
            Map<String, Object> headers = new HashMap<String, Object>();
            Map sendHeaders = new HashMap();
            sendHeaders.put("nativeHeaders", headers);

            headers.put("response-code", Arrays.asList(new String[]{String.valueOf(1)}));
            headers.put("response-message", Arrays.asList(new String[]{"success"}));

            messagingTemplate.convertAndSendToUser(user, destination, payload, sendHeaders);
        }
    }

    public void putUser(String uid, String name) {
        uidMap.put(uid , name);
    }
}
