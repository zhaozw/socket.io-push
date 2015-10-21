package com.yy.httpproxy.socketio;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.yy.httpproxy.AndroidLoggingHandler;
import com.yy.httpproxy.requester.HttpRequester;
import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.subscribe.PushCallback;
import com.yy.httpproxy.subscribe.PushGenerator;
import com.yy.httpproxy.subscribe.Pusher;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoClient implements HttpRequester, Pusher {

    private static String TAG = "SocketIoRequester";
    private PushCallback pushCallback;
    private PushGenerator pushGenerator;

    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (pushGenerator != null) {
                JSONObject object = new JSONObject();
                try {
                    String pushId = pushGenerator.generatePushId();
                    if (pushId != null) {
                        object.put("id", pushId);
                    }
                    mSocket.emit("pushId", object);
                } catch (JSONException e) {
                    Log.e(TAG, "connectListener error ", e);
                }
            }
        }
    };
    private final Emitter.Listener pushIdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (pushGenerator != null) {
                JSONObject data = (JSONObject) args[0];
                String pushId = data.optString("id");
                Log.v(TAG, "on pushId " + pushId);
            }
        }
    };

    private final Emitter.Listener pushListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (pushCallback != null) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String topic = data.optString("topic");
                    String dataBase64 = data.optString("data");
                    Log.v(TAG, "on push topic " + topic + " data:" + dataBase64);
                    pushCallback.onPush(topic, Base64.decode(dataBase64, Base64.DEFAULT));
                } catch (Exception e) {
                    Log.e(TAG, "handle push error ", e);
                }
            }
        }
    };
    private Map<Integer, Request> replyCallbacks = new ConcurrentHashMap<>();
    private Handler handler = new Handler();
    private long timeout = 20000;
    private Set<String> topics = new HashSet<>();
    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            Iterator<Map.Entry<Integer, Request>> it = replyCallbacks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Request> pair = it.next();
                Request request = pair.getValue();
                if (request.timeoutForRequest(timeout)) {
                    Log.i(TAG, "StompClient timeoutForRequest " + request.getRequestInfo().getUrl());
                    if (request.getResponseHandler() != null) {
                        request.getResponseHandler().onError(new RequestException(null, RequestException.Error.TIMEOUT_ERROR));
                    }
                    it.remove();
                    continue;
                }
            }
            postTimeout();
        }
    };
    private int sequenceId;

    private void postTimeout() {
        handler.removeCallbacks(timeoutTask);
        if (replyCallbacks.size() > 0) {
            handler.postDelayed(timeoutTask, 1000);
        }
    }

    private final Emitter.Listener httpProxyListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v(TAG, "httpProxy call " + args + " thread " + Thread.currentThread().getName());
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject data = (JSONObject) args[0];
                int responseSeqId = data.optInt("sequenceId", 0);
                Request request = replyCallbacks.remove(responseSeqId);
                if (request != null && request.getResponseHandler() != null) {
                    try {
                        String errorMessage = data.optString("errorMessage");
                        boolean error = data.optBoolean("error", false);
                        if (error) {
                            request.getResponseHandler().onError(new RequestException(null, RequestException.Error.CONNECT_ERROR.value, errorMessage));
                        } else {
                            String response = data.optString("response");
                            byte[] decodedResponse = Base64.decode(response, Base64.DEFAULT);
                            Log.i(TAG, "response " + new String(decodedResponse));
                            int statusCode = data.optInt("statusCode", 0);
                            Map<String, String> headers = new HashMap<>();
                            JSONObject headerObject = data.optJSONObject("headers");
                            Iterator<String> it = headerObject.keys();
                            while (it.hasNext()) {
                                String key = it.next();
                                headers.put(key, headerObject.optString(key));
                            }
                            request.getResponseHandler().onSuccess(headers, statusCode, decodedResponse);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "httpproxy emmit parse error", e);
                        request.getResponseHandler().onError(new RequestException(e, RequestException.Error.SERVER_DATA_SERIALIZE_ERROR));
                    }
                }
            }
        }
    };

    private Socket mSocket;

    public SocketIoClient(String host) {
        AndroidLoggingHandler.reset(new AndroidLoggingHandler());
        java.util.logging.Logger.getLogger("").setLevel(Level.FINEST);
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{"websocket"};
            mSocket = IO.socket(host, opts);
            mSocket.on("httpProxy", httpProxyListener);
            mSocket.on(Socket.EVENT_CONNECT, connectListener);
            mSocket.on("pushId", pushIdListener);
            mSocket.on("push", pushListener);
            mSocket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void request(RequestInfo requestInfo, ResponseHandler handler) {

        try {

            if (!mSocket.connected()) {
                handler.onError(new RequestException(null, RequestException.Error.CONNECT_ERROR));
                return;
            }

            sequenceId = sequenceId + 1;
            JSONObject headers = new JSONObject();
            if (requestInfo.getHeaders() != null) {
                for (Map.Entry<String, String> header : requestInfo.getHeaders().entrySet()) {
                    headers.put(header.getKey(), header.getValue());
                }
            }

            JSONObject object = new JSONObject();
            object.put("headers", headers);
            object.put("body", Base64.encode(requestInfo.getBody(), Base64.DEFAULT));
            object.put("host", requestInfo.getHost());
            object.put("port", requestInfo.getPort());
            object.put("method", requestInfo.getMethod());
            object.put("path", requestInfo.getPath());
            object.put("sequenceId", String.valueOf(sequenceId));

            Request request = new Request(requestInfo, handler);

            replyCallbacks.put(sequenceId, request);
            mSocket.emit("httpProxy", object);

            postTimeout();


        } catch (Exception e) {
            handler.onError(new RequestException(e, RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR));
        }
    }

    @Override
    public void setPushGenerator(PushGenerator pushGenerator) {
        this.pushGenerator = pushGenerator;
    }

    @Override
    public void subscribe(String topic) {
    }

    @Override
    public void setPushCallback(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }
}
