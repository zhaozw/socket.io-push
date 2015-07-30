package com.yy.androidlib.websocket;

import android.os.Build;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFactory {

    private static final String TAG = "WebSocketFactory";

    public static IWebSocketClient local(String url, WebSocketListener listener, HostResolver hostResolver) {
        try {
            String deveice = Build.MODEL;
            Log.i(TAG, "device :" + deveice);
            if ("HM NOTE 1LTE".equals(deveice)) {
                Log.i(TAG, "HM NOTE use java.net client");
                return new JavaNetWebSocketClient(new URI(url), listener);
            } else {
                return new NioWebSocketClient(new URI(url), listener, hostResolver);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
