package com.yy.androidlib.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.yy.androidlib.hiido.profiling.HiidoProfiling;
import com.yy.androidlib.hiido.profiling.ProfileData;
import com.yy.androidlib.util.apache.RandomStringUtils;
import com.yy.androidlib.util.http.Profiler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MisakaClient implements StompConnectManager.StompListener {

    private Config config;
    private Handler mainHandler;
    private Set<Callback> callbacks = new HashSet<Callback>();
    private Map<String, Request> replyCallbacks = new HashMap<String, Request>();
    private Map<String, ReplyHandler> subscribeCallbacks = new HashMap<String, ReplyHandler>();
    private AntPathMatcher matcher = new AntPathMatcher();
    private Router router = new Router();
    private Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer() {
        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : new Date(json.getAsLong());
        }
    }).registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? new JsonPrimitive(0l) : new JsonPrimitive(src.getTime());
        }
    }).create();

    private static final String TAG = "Stomp";
    private StompConnectManager stomp;
    private HiidoProfiling profiling; //海度上报

    @Override
    public void onConnected() {
        for (Callback callback : callbacks) {
            callback.onConnected();
        }
        if (!replyCallbacks.isEmpty()) {
            List<Request> values = new ArrayList<Request>(replyCallbacks.values());
            replyCallbacks.clear();
            for (Request request : values) {
                Log.i(TAG, "StompClient onConnected repost request " + request.getDestination());
                request(request.getAppId(), request.getDestination(), request.getBody(), request.getReplyHandler());
            }
        }
    }

    @Override
    public void onDisconnected(String reason, Exception e) {
        for (Callback callback : callbacks) {
            callback.onDisconnected(reason, e);
        }
    }

    @Override
    public void onMessage(Message message) {
        String path = message.getDestination();
        Destination destination = new Destination(matcher, path);
        String requestId = message.getHeaders().get("request-id");
        String code = message.getHeaders().get("response-code");
        Log.d(TAG, "onReceive path: " + path + " message : " + message.getBody());
        int respCode = -1;
        if (code != null) {
            try {
                respCode = Integer.parseInt(code);
            } catch (Exception e) {
                Log.e(TAG, "parse code error");
            }
        }

        String msg = message.getHeaders().get("response-message");
        if (msg == null) {
            msg = "";
        }
        //从map中删除id
        if (path.equals("/user/queue/reply")) {
            Request request = replyCallbacks.remove(requestId);
            if (request != null && request.getReplyHandler() != null) {
                request.getReplyHandler().handle(gson, message.getBody(), respCode, msg, config.getServerDataParseErrorTips());
            }
            if (profiling != null) {
                int status;
                if (respCode == 1) {
                    status = Profiler
                            .Status.SUCCESS.value;
                } else { // fail
                    status = Profiler.Status.ERROR.value;
                }
                report(request, status);
            }
        } else {
            for (Map.Entry<String, ReplyHandler> entry : subscribeCallbacks.entrySet()) {
                if (destination.matches(entry.getKey())) {
                    entry.getValue().handle(gson, message.getBody(), respCode, msg, config.getServerDataParseErrorTips());
                }
            }
        }
    }

    public void addRoute(String appId, String url) {
        router.addRoute(appId, url);
    }

    public static abstract class SubscribeHandler<T> extends ReplyHandler<T> {

        public SubscribeHandler(Class clazz) {
            super(clazz);
        }

        @Override
        public void onError(int code, String message) {

        }
    }

    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            Iterator<Map.Entry<String, Request>> it = replyCallbacks.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Request> pair = it.next();
                Request request = pair.getValue();
                if (request.timeoutForRequest()) {
                    Log.i(TAG, "StompClient timeoutForRequest " + request.getDestination());
                    if (request.getReplyHandler() != null) {
                        request.getReplyHandler().onError(-2, config.getServerReplyTimeOutTips());
                    }
                    it.remove();
                    continue;
                }
                if (request.timeoutForReconnect() && stomp.isConnected()) {
                    Log.i(TAG, "StompClient timeoutForReconnect reconnect " + request.getDestination());
                    stomp.reconnect();
                }
            }
            mainHandler.removeCallbacks(this);
            mainHandler.postDelayed(this, 1000);
        }
    };

    public MisakaClient(Context context, String host, Config config) {
        this.config = config;
        mainHandler = new Handler(Looper.getMainLooper());
        stomp = new StompConnectManager(context, config, mainHandler, host, this);
        mainHandler.post(timeoutTask);
    }

    public void request(String appId, String destination, Object body, final ReplyHandler replyHandler) {
        String url = router.getRequestUrl(appId, destination);
        String json;
        if (body != null) {
            if (body instanceof String) {
                json = body.toString();
            } else {
                json = gson.toJson(body);
            }
        } else {
            json = "";
        }
        if (stomp.isConnected()) {
            String[] headers;
            if (config.isDataAsBody()) {
                headers = new String[]{"url", url, "appId", appId, "dataAsBody", "true"};
            } else {
                headers = new String[]{"url", url, "appId", appId};
            }
            int key = stomp.send("/request", json, headers);
            if (key > 0) {
                if (replyHandler != null) {
                    final String keyStr = String.valueOf(key);
                    replyCallbacks.put(keyStr, new Request(appId, destination, body, replyHandler));
                }
            } else {
                if (replyHandler != null) {
                    replyHandler.onError(-1, config.getCannotConnectToServerTips());
                }
            }
        } else {
            //等待连接完成再发请求
            replyCallbacks.put(RandomStringUtils.randomAlphanumeric(12), new Request(appId, destination, body, replyHandler));
        }
    }

    public void subscribeBroadcast(String pushId, ReplyHandler handler) {
        subscribe("/topic/" + pushId, handler);
    }

    public void subscribeUserPush(String pushId, ReplyHandler handler) {
        subscribe("/user/queue/" + pushId, handler);
    }


    private void subscribe(String destination, ReplyHandler handler) {
        stomp.subscribe(destination);
        subscribeCallbacks.put(destination, handler);
    }

    public void addConnectionCallback(Callback callback) {
        callbacks.add(callback);
    }


    public void setProfiling(HiidoProfiling profiling) {
        this.profiling = profiling;
    }

    public void enableProfiling(HiidoProfiling profiling) {
        setProfiling(profiling);
    }

    private void report(Request request, int status) {
        if (this.profiling == null || request == null) {
            return;
        }
        String appId = request.getAppId();
        String interfaceName = request.getDestination();
        int _appId;
        if (appId != null || "".equals(appId)) {
            _appId = 0;
        } else {
            try {
                _appId = Integer.parseInt(appId);
            } catch (Exception e) {
                _appId = 0;
            }
        }
        profiling.setAppId(_appId);
        long startTime = request.getTimestamp();
        profiling.report(startTime, interfaceName, status);

    }


}
