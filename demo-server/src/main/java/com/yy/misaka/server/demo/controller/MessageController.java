package com.yy.misaka.server.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.misaka.server.demo.domain.Message;
import com.yy.misaka.server.demo.domain.User;
import com.yy.misaka.server.demo.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class MessageController extends BaseController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/enterRoom")
    public
    @ResponseBody
    List<User> enterRoom(@RequestParam(required = false) String data, @RequestParam String appId, @RequestHeader("X-Uid") String uid) {
        logger.info("data {}, appId {}", data, appId);
        messageService.enterRoom(uid);
//        throw new ResultException(-5, "111");
        return messageService.allUsers();
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendMessage(@RequestParam String data, @RequestParam String appId, @RequestHeader("X-Uid") String uid) throws IOException {
        logger.info("data {}, appId {}", data, appId);
        Message message = mapper.readValue(data, Message.class);
        message.setFromUid(uid);
        messageService.sendMessage(message);
        return message;
    }

}