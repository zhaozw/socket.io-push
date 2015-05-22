package com.yy.misaka.server.demo.controller;

import com.yy.misaka.server.demo.util.BS2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class UploadController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private BS2Util bs2Util;


    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public
    @ResponseBody
    String uploadFileHandler(@RequestParam("file") MultipartFile file) throws IOException {
        logger.info("uploadFile {}", file.getSize());
        String url = bs2Util.uploadFile(file);
        return url;
    }

}