package com.yy.misaka.server.lbs.socketio;

public class ProxyRequest {

    private String url;
    private String data;
    private int sequenceId;

    public ProxyRequest() {
    }

    public ProxyRequest(String userName, String data) {
        super();
        this.url = userName;
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }
}
