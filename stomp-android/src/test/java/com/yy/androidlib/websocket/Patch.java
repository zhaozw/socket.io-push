package com.yy.androidlib.websocket;

import java.util.Date;

public class Patch {

    private String appId;
    private String id;
    private String version;
    private byte[] dx;
    private String deviceIds;
    private String rule;
    private int hashRatio;
    private Date createTime = new Date();

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Patch{");
        sb.append("version='").append(version).append('\'');
        sb.append(", appId='").append(appId).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getDx() {
        return dx;
    }

    public void setDx(byte[] dx) {
        this.dx = dx;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(String deviceIds) {
        this.deviceIds = deviceIds;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDexUrl() {
        return "http://bbs.yypm.com:81/patches/" + appId + "/" + getId() + ".dex";
    }

    public void setDexUrl(String dexUrl) {
    }

    public boolean eligibleForMine(String deviceId, String version) {
        if (!version.equals(this.version)) {
            return false;
        }
        if (rule.equals("all")) {
            return true;
        } else if (rule.equals("deviceId") && deviceIds.contains(deviceId)) {
            return true;
        } else if (rule.equals("hash") && deviceId.hashCode() % hashRatio == 0) {
            return true;
        }
        return false;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public int getHashRatio() {
        return hashRatio;
    }

    public void setHashRatio(int hashRatio) {
        this.hashRatio = hashRatio;
    }
}
