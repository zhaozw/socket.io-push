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
    List<User> enterRoom(@RequestHeader("X-Uid") String uid) {
        messageService.enterRoom(uid);
        return messageService.allUsers();
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendMessage(@RequestBody Message message, @RequestHeader("X-Uid") String uid) throws IOException {
        message.setFromUid(uid);
        messageService.sendMessage(message);
        return message;
    }

}