package com.yy.androidlib.websocket;

import android.util.Log;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;
import com.yy.androidlib.util.apache.RandomStringUtils;

import java.net.URI;
import java.util.concurrent.Future;

public class NioWebSocketClient implements IWebSocketClient, AsyncHttpClient.WebSocketConnectCallback, WebSocket.StringCallback, CompletedCallback {

    private static String TAG = "NioWebSocketClient";
    private WebSocket webSocket;
    private Future<WebSocket> webSocketFuture;
    private URI mURI;
    private WebSocketListener mListener;
    private HostResolver hostResolver;

    public NioWebSocketClient(URI uri, WebSocketListener listener,HostResolver hostResolver) {
        mURI = uri;
        mListener = listener;
        this.hostResolver = hostResolver;
    }

    public void connect() {
        String url = mURI.toString();
        if (hostResolver != null) {
            String host = mURI.getHost();
            String resolved = hostResolver.resolve(host);
            url = url.replace(host, resolved);
        }
        url = url + "/stomp/1/" + RandomStringUtils.randomAlphanumeric(32) + "/websocket";
        AsyncHttpGet get = new AsyncHttpGet(url);
        Log.v(TAG, "AndroidAsyncClient connect " + url);
        get.setTimeout(4000);
        webSocketFuture = AsyncHttpClient.getDefaultInstance().websocket(get, null, NioWebSocketClient.this);
        if (webSocket != null) {
            webSocket.setStringCallback(null);
            webSocket.setClosedCallback(null);
        }
    }

    @Override
    public boolean isDisconnected() {
        return !(webSocketFuture != null && !webSocketFuture.isDone()) && !(webSocket != null && webSocket.isOpen());
    }

    @Override
    public boolean send(String data) {
        if (webSocket != null && webSocket.isOpen()) {
            Log.d(TAG, "sendCommand \n" + data);
            webSocket.send(data);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onCompleted(final Exception ex, final WebSocket ws) {
        webSocket = ws;
        Log.i(TAG, "websocket onCompleted " + webSocket + " thread " + Thread.currentThread().getName());
        if (ex != null || ws == null) {
            Log.d(TAG, "onCompleted error", ex);
            if (ex != null) {
                mListener.onDisconnect(ex.getMessage(), ex);
            } else {
                mListener.onDisconnect("no ex thrown", new IllegalStateException("no ex thrown"));
            }
        } else {
            webSocket.setStringCallback(NioWebSocketClient.this);
            webSocket.setClosedCallback(NioWebSocketClient.this);
        }
    }

    @Override
    public void onStringAvailable(String s) {
        Log.i(TAG, "onStringAvailable " + s);
        if ("o".equals(s)) {
            mListener.onConnect();
        } else if (s.startsWith("c[\"")) {
            Log.i(TAG, "receive close command " + s);
            close();
            return;
        } else if (s.startsWith("a[\"")) {
            mListener.onMessage(s.substring(3, s.length() - 1));
        }
        mListener.onHeartbeat();
    }

    @Override
    public void onCompleted(Exception ex) {
        if (webSocket != null) {
            Log.e(TAG, "closed callback ", ex);
            close();
        }
    }

    @Override
    public void close() {
        try {
            if (webSocket != null) {
                webSocket.setStringCallback(null);
                webSocket.setClosedCallback(null);
                webSocket.close();
                webSocket = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "close websocket exception ", e);
        }

    }
}