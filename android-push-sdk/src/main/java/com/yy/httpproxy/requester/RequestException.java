package com.yy.httpproxy.requester;

public class RequestException extends Exception {

    public RequestException(Exception e, int code, String msg) {
        super(msg, e);
        this.code = code;
        this.message = msg;
    }

    public enum Error {

        TIMEOUT_ERROR(-10001), CONNECT_ERROR(-10002), SERVER_DATA_SERIALIZE_ERROR(-10003), CLIENT_DATA_SERIALIZE_ERROR(-10003);

        public int value;

        private Error(int value) {
            this.value = value;
        }
    }

    private String message;
    private int code;

    public RequestException(Throwable cause, Error error) {
        super(error.name(), cause);
        this.message = error.name();
        this.code = error.value;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
