package com.yy.misaka.server.lbs.socketio;

public class ProxyResponse {

    private String url;
    private String response;
    private int sequenceId;
    private int responseCode = 1;
    private String reponseMessage;

    public ProxyResponse() {
    }

    public ProxyResponse(String userName, String response) {
        super();
        this.url = userName;
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getReponseMessage() {
        return reponseMessage;
    }

    public void setReponseMessage(String reponseMessage) {
        this.reponseMessage = reponseMessage;
    }
}
