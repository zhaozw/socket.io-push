package com.yy.misaka.server.demo.util;

import org.apache.commons.lang.RandomStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by xuduo on 1/22/15.
 */
public class Sms {

    public static void send(String phone, String msg) {
        String encode = null;
        try {
            encode = URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {

        }
        String url = "http://gossip.sysop.duowan.com:9900/?appId=79&appKey=SLEEPAPPMSG&mobile=" + phone + "&message=" + encode + "&muid=" + RandomStringUtils.randomNumeric(16);
        HttpUtil.getUrlAsString(url);
    }
}
