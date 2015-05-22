package com.yy.androidlib.websocket;

import java.util.Map;

public class Destination {

    private String destination;
    private AntPathMatcher matcher;

    public Destination(AntPathMatcher matcher, String destination) {
        this.destination = destination;
        this.matcher = matcher;
    }

    public boolean matches(String pattern) {
        return matcher.match(pattern, destination);
    }


    public String getParam(String pattern, String key) {
        Map<String, String> params = matcher.extractUriTemplateVariables(pattern, key);
        String value = params.get(key);
        return value == null ? "" : value;
    }
}
