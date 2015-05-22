package com.yy.misaka.server.demo.controller;

import com.yy.misaka.support.ResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BaseController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(ResultException.class)
    public ResponseEntity<String> exception(ResultException e) {
        logger.error("test exception ", e);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        List<String> code = new ArrayList<String>();
        code.add(e.getResult().getCode() + "");
        headers.put("response-code", code);
        List<String> message = new ArrayList<String>();
        message.add(e.getResult().getMessage());
        headers.put("response-message", message);
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }


}