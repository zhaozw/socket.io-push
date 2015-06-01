package com.yy.androidlib.websocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuduo on 3/3/15.
 */
public class Router {

    private Map<String, String> routes = new HashMap<String, String>();

    public void addRoute(String appId, String url) {
        routes.put(appId, url);
    }

    public String getRequestUrl(String appId, String path) {
        return routes.get(appId) + path;
    }

}
