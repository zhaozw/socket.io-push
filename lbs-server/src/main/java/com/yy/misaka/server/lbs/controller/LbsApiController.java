package com.yy.misaka.server.lbs.controller;

import com.yy.misaka.server.lbs.domain.User;
import com.yy.misaka.server.support.BaseMessageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LbsApiController extends BaseMessageController {


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public
    @ResponseBody
    User simple(User data) {
        logger.info("data {} sessionId {}", data);
        User user = new User();
        user.setUsername("user");
        user.setPassword("password");
        return user;
    }

    @RequestMapping(value = "/api/broadcast")
    public ResponseEntity<String> broadcast(String destination, String pushId, String data) {
        messagingTemplate.broadcast("/topic/" + pushId + destination, data);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value = "/api/pushToUser")
    public ResponseEntity<String> pushToUser(String destination, String pushId, String uid, String data) {
        messagingTemplate.sendToUser(uid, "/queue/" + pushId + destination, data);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

}