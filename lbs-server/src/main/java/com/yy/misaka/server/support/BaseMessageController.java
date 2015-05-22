package com.yy.misaka.server.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;

public class BaseMessageController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected ReplyMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(ServiceException.class)
    public void exception(Message<Object> message, ServiceException e) {
        logger.error("controller service exception ", e);
        messagingTemplate.replyToUser(message, null, e.getCode(), e.getMessage());
    }

}
