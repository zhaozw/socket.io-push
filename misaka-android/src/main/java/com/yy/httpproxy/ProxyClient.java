package com.yy.httpproxy;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.requester.ResponseHandler;
import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.subscribe.PushCallback;

import java.util.HashMap;
import java.util.Map;

public class ProxyClient implements PushCallback {

    private Config config;
    public static final String TAG = "ProxyClient";
    private long mainThreadId = Looper.getMainLooper().getThread().getId();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, ReplyHandler> pushHandlers = new HashMap<>();

    public ProxyClient(Config config) {
        this.config = config;
        if (config.getPushSubscriber() != null) {
            config.getPushSubscriber().setPushCallback(this);
        }
    }

    public void request(String method, String scheme, String host, int port, String path, Map<String, String> headers, Object body, final ReplyHandler replyHandler) {
        final RequestInfo requestInfo = new RequestInfo();
        requestInfo.setBody(config.getRequestSerializer().toBinary(path, body));
        requestInfo.setHeaders(headers);
        requestInfo.setHost(host);
        requestInfo.setPath(path);
        requestInfo.setPort(port);
        requestInfo.setMethod(method);

        config.getRequester().
                request(requestInfo, new ResponseHandler() {
                    @Override
                    public void onSuccess(Map<String, String> headers, int statusCode, byte[] body) {
                        try {
                            Object result = config.getRequestSerializer().toObject(replyHandler.clazz, statusCode, headers, body);
                            callSuccessOnMainThread(replyHandler, result);
                        } catch (RequestException e) {
                            Log.e(TAG, "serialize exception " + e.getMessage(), e);
                            callErrorOnMainThread(replyHandler, e);
                        }
                    }

                    @Override
                    public void onError(RequestException e) {
                        Log.e(TAG, "serialize exception " + e.getMessage(), e);
                        callErrorOnMainThread(replyHandler, e);
                    }
                });
    }

    public void subscribe(String topic, ReplyHandler handler) {
        pushHandlers.put(topic, handler);
    }

    public void subscribeBroadcast(String topic, ReplyHandler handler) {
        pushHandlers.put(topic, handler);
        config.getPushSubscriber().subscribeBroadcast(topic);
    }

    @Override
    public void onPush(String topic, byte[] data) {
        ReplyHandler handler = pushHandlers.get(topic);
        if (handler != null) {
            Object result;
            if (config.getPushSerializer() != null) {
                try {
                    result = config.getPushSerializer().toObject(topic, handler.clazz, data);
                } catch (Exception e) {
                    Log.e(TAG, "onPush serialize exception " + e.getMessage(), e);
                    return;
                }
            } else {
                result = data;
            }
            callSuccessOnMainThread(handler, result);
        }
    }

    private void callSuccessOnMainThread(final ReplyHandler replyHandler, final Object result) {
        if (Thread.currentThread().getId() == mainThreadId) {
            replyHandler.onSuccess(result);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    replyHandler.onSuccess(result);
                }
            });
        }
    }

    private void callErrorOnMainThread(final ReplyHandler replyHandler, final RequestException e) {
        if (Thread.currentThread().getId() == mainThreadId) {
            replyHandler.onError(e.getCode(), e.getMessage());
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    replyHandler.onError(e.getCode(), e.getMessage());
                }
            });
        }
    }


    public void setPushId(String pushId) {
         config.getPushSubscriber().setPushId(pushId);
    }
}
