package com.yy.androidlib.websocket;

public class Config {

    private String cannotConnectToServerTips = "无法连接服务器!";
    private String serverDataParseErrorTips = "解析服务器数据失败!";
    private String serverReplyTimeOutTips = "请求服务器超时!";
    private long serverReplyTimeOut = 10000L;
    private Mode mode = Mode.LOCAL;
    private boolean useNyy = true;
    private HostResolver hostResolver;
    private NyyLocalRequest nyyLocalRequest;

    public Config useNyy(boolean useNyy) {
        this.useNyy = useNyy;
        return this;
    }
    public enum Mode {LOCAL, REMOTE}

    public NyyLocalRequest getNyyLocalRequest() {
        return nyyLocalRequest;
    }

    public String getCannotConnectToServerTips() {
        return cannotConnectToServerTips;
    }

    public void setCannotConnectToServerTips(String cannotConnectToServerTips) {
        this.cannotConnectToServerTips = cannotConnectToServerTips;
    }

    public String getServerDataParseErrorTips() {
        return serverDataParseErrorTips;
    }

    public void setServerDataParseErrorTips(String serverDataParseErrorTips) {
        this.serverDataParseErrorTips = serverDataParseErrorTips;
    }

    public String getServerReplyTimeOutTips() {
        return serverReplyTimeOutTips;
    }

    public void setServerReplyTimeOutTips(String serverReplyTimeOutTips) {
        this.serverReplyTimeOutTips = serverReplyTimeOutTips;
    }

    public long getServerReplyTimeOut() {
        return serverReplyTimeOut;
    }

    public void setServerReplyTimeOut(long serverReplyTimeOut) {
        this.serverReplyTimeOut = serverReplyTimeOut;
    }

    public Config mode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public Config hostResolver(HostResolver hostResolver) {
        this.hostResolver = hostResolver;
        return this;
    }

    public Config nyyLocalRequest(NyyLocalRequest nyyLocalRequest) {
        this.nyyLocalRequest = nyyLocalRequest;
        return this;
    }

    public HostResolver getHostResolver() {
        return hostResolver;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isUseNyy() {
        return useNyy;
    }

    public void setUseNyy(boolean useNyy) {
        this.useNyy = useNyy;
    }
}
