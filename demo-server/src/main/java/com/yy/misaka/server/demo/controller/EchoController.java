package com.yy.misaka.server.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class EchoController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)
    public void exception(Exception e) {
        logger.error("test exception ", e);
    }

    @RequestMapping(value = "/echo",method = RequestMethod.POST)
    public
    @ResponseBody
    String test(@RequestParam String data, @RequestParam String appId) {
        logger.info("data {}, appId {}", data, appId);
        return data;
    }

}