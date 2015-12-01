package com.yy.httpproxy.socketio;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.yy.httpproxy.AndroidLoggingHandler;
import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.subscribe.PushCallback;
import com.yy.httpproxy.subscribe.PushSubscriber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketIOProxyClient implements PushSubscriber {

    private static String TAG = "SocketIoRequester";
    private PushCallback pushCallback;
    private String pushId;
    private NotificationCallback notificationCallback;
    private Set<String> topics = new HashSet<>();
    private ResponseHandler responseHandler;

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void unsubscribeBroadcast(String topic) {
        topics.remove(topic);
        if (socket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("topic", topic);
                socket.emit("unsubscribeTopic", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public interface NotificationCallback {
        void onNotification(String id, JSONObject notification);
    }

    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendPushIdAndTopicToServer();
            reSendFailedRequest();
        }
    };

    private void reSendFailedRequest() {
        if (!replyCallbacks.isEmpty()) {
            List<RequestInfo> values = new ArrayList<>(replyCallbacks.values());
            replyCallbacks.clear();
            for (RequestInfo request : values) {
                Log.i(TAG, "StompClient onConnected repost request " + request.getUrl());
                request(request);
            }
        }
    }

    private void sendPushIdAndTopicToServer() {
        if (pushId != null && socket.connected()) {
            Log.i(TAG, "sendPushIdAndTopicToServer " + pushId);
            JSONObject object = new JSONObject();
            try {
                object.put("id", pushId);
                if (topics.size() > 0) {
                    JSONArray array = new JSONArray();
                    object.put("topics", array);
                    for (String topic : topics) {
                        array.put(topic);
                    }
                }
                socket.emit("pushId", object);
            } catch (JSONException e) {
                Log.e(TAG, "connectListener error ", e);
            }
        }
    }

    private final Emitter.Listener pushIdListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String pushId = data.optString("id");
            Log.v(TAG, "on pushId " + pushId);
        }
    };

    private final Emitter.Listener notificationListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (notificationCallback != null) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    JSONObject android = data.optJSONObject("android");
                    Log.v(TAG, "on notification topic " + android);
                    notificationCallback.onNotification(data.optString("id"), android);
                } catch (Exception e) {
                    Log.e(TAG, "handle notification error ", e);
                }
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
                    boolean reply = data.optBoolean("reply", false);
                    if (reply) {
                        data.put("pushId", pushId);
                        data.remove("data");
                        socket.emit("pushReply", data);
                    }
                    Log.v(TAG, "on push topic " + topic + " data:" + dataBase64);
                    pushCallback.onPush(topic, Base64.decode(dataBase64, Base64.DEFAULT));
                } catch (Exception e) {
                    Log.e(TAG, "handle push error ", e);
                }
            }
        }
    };
    private Map<Integer, RequestInfo> replyCallbacks = new ConcurrentHashMap<>();
    private Handler handler = new Handler();
    private long timeout = 20000;
    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            Iterator<Map.Entry<Integer, RequestInfo>> it = replyCallbacks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, RequestInfo> pair = it.next();
                RequestInfo request = pair.getValue();
                if (request.timeoutForRequest(timeout)) {
                    Log.i(TAG, "StompClient timeoutForRequest " + request.getUrl());
                    if (responseHandler != null) {
                        responseHandler.onResponse(request.getSequenceId(), RequestException.Error.TIMEOUT_ERROR.value, RequestException.Error.TIMEOUT_ERROR.name(), null);
                    }
                    it.remove();
                    continue;
                }
            }
            postTimeout();
        }
    };

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
                RequestInfo request = replyCallbacks.remove(responseSeqId);
                if (request != null && responseHandler != null) {
                    String response = data.optString("data");
                    byte[] decodedResponse = Base64.decode(response, Base64.DEFAULT);
                    Log.i(TAG, "response " + new String(decodedResponse));
                    int code = data.optInt("code", 0);
                    int sequenceId = data.optInt("sequenceId", 0);
                    String message = data.optString("message", "");
                    responseHandler.onResponse(sequenceId, code, message, decodedResponse);
                }
            }
        }
    };

    private Socket socket;

    public SocketIOProxyClient(String host) {
        AndroidLoggingHandler.reset(new AndroidLoggingHandler());
        java.util.logging.Logger.getLogger("").setLevel(Level.FINEST);
        topics.add("android");
        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{"websocket"};
            socket = IO.socket(host, opts);
            socket.on("packetProxy", httpProxyListener);
            socket.on(Socket.EVENT_CONNECT, connectListener);
            socket.on("pushId", pushIdListener);
            socket.on("push", pushListener);
            socket.on("notification", notificationListener);
            socket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    public void request(RequestInfo requestInfo) {

        try {

            if (handler != null) {
                replyCallbacks.put(requestInfo.getSequenceId(), requestInfo);
            }

            if (!socket.connected()) {
                return;
            }


            JSONObject headers = new JSONObject();
            if (requestInfo.getHeaders() != null) {
                for (Map.Entry<String, String> header : requestInfo.getHeaders().entrySet()) {
                    headers.put(header.getKey(), header.getValue());
                }
            }

            JSONObject object = new JSONObject();
            object.put("headers", headers);
            object.put("data", Base64.encodeToString(requestInfo.getBody(), Base64.DEFAULT));
            object.put("host", requestInfo.getHost());
            object.put("port", requestInfo.getPort());
            object.put("method", requestInfo.getMethod());
            object.put("path", requestInfo.getPath());
            object.put("sequenceId", String.valueOf(requestInfo.getSequenceId()));


            socket.emit("packetProxy", object);

            postTimeout();


        } catch (Exception e) {
            responseHandler.onResponse(requestInfo.getSequenceId(), RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.value, RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.name(), null);
        }
    }

    @Override
    public void subscribeBroadcast(String topic) {
        topics.add(topic);
        if (socket.connected()) {
            JSONObject data = new JSONObject();
            try {
                data.put("topic", topic);
                socket.emit("subscribeTopic", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPushCallback(PushCallback pushCallback) {
        this.pushCallback = pushCallback;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
        sendPushIdAndTopicToServer();
    }

    public void setNotificationCallback(NotificationCallback notificationCallback) {
        this.notificationCallback = notificationCallback;
    }
}
