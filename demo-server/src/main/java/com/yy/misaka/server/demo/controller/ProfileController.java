package com.yy.misaka.server.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yy.misaka.server.demo.domain.User;
import com.yy.misaka.server.demo.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class ProfileController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ProfileService profileService;

    @ExceptionHandler(Exception.class)
    public void exception(Exception e) {
        logger.error("test exception ", e);
    }

    @RequestMapping(value = "/modifyMyInfo", method = RequestMethod.POST)
    public
    @ResponseBody
    User modify(@RequestParam String data, @RequestParam String appId, @RequestHeader("X-Uid") String uid) throws IOException {
        logger.info("data {}, appId {}", data, appId);
        User user = mapper.readValue(data, User.class);
        user.setUid(uid);
        return profileService.modify(user);
    }

    @RequestMapping(value = "/myInfo", method = RequestMethod.POST)
    public
    @ResponseBody
    User myInfo(@RequestParam String data, @RequestParam String appId, @RequestHeader("X-Uid") String uid) throws IOException {
        logger.info("data {}, appId {}", data, appId);
        return profileService.getByUid(uid);
    }

}