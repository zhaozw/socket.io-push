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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BaseController {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exception(Exception e) {
        logger.error("Controller exception ", e);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        List<String> message = new ArrayList<String>();
        List<String> code = new ArrayList<String>();
        if (e instanceof ResultException) {
            ResultException resultException = (ResultException) e;
            code.add(resultException.getResult().getCode() + "");
            try {
                message.add(URLEncoder.encode(resultException.getResult().getMessage(), "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                message.add("");
            }
        } else {
            code.add("-1");
            message.add(e.getMessage());
        }
        headers.put("response-code", code);
        headers.put("response-message", message);
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }

}