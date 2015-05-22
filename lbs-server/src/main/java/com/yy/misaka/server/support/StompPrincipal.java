package com.yy.misaka.server.support;

import org.apache.commons.lang.RandomStringUtils;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class StompPrincipal implements Principal {

    private String name = RandomStringUtils.randomAlphanumeric(32);
    private String loginAppId = "0";
    private String userId = "0";
    private String remoteIp = "";
    private Map<String, String> misakaHeaders = new HashMap<String, String>();

    public StompPrincipal() {

    }

    @Override
    public String getName() {
        return name;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getLoginAppId() {
        return loginAppId;
    }

    public void setLoginAppId(String loginAppId) {
        this.loginAppId = loginAppId;
    }

    public void addHeader(String key, String value) {
        misakaHeaders.put(key, value);
    }

    public Map<String, String> headers() {
        return misakaHeaders;
    }
}
