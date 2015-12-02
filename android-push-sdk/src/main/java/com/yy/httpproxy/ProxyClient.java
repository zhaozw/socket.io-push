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
    private Map<Integer, ReplyHandler> replayHandlers = new HashMap<>();
    private int sequenceId;

    public ProxyClient(Config config) {
        this.config = config;
        if (config.getRemoteClient() != null) {
            config.getRemoteClient().setProxyClient(this);
        }
    }

    public void request(String method, String scheme, String host, int port, String path, Map<String, String> headers, Object body, final ReplyHandler replyHandler) {
        final RequestInfo requestInfo = new RequestInfo();
        requestInfo.setScheme(scheme);
        requestInfo.setBody(config.getRequestSerializer().toBinary(path, body));
        requestInfo.setHeaders(headers);
        requestInfo.setHost(host);
        requestInfo.setPath(path);
        requestInfo.setPort(port);
        requestInfo.setMethod(method);
        requestInfo.setSequenceId(sequenceId++);

        if (replyHandler != null) {
            requestInfo.setExpectReply(true);
            replayHandlers.put(sequenceId, replyHandler);
        }
        requestInfo.setSequenceId(sequenceId);

        config.getRemoteClient().
                request(requestInfo);
    }

    public void subscribe(String topic, ReplyHandler handler) {
        pushHandlers.put(topic, handler);
    }

    public void subscribeBroadcast(String topic, ReplyHandler handler) {
        pushHandlers.put(topic, handler);
        config.getRemoteClient().subscribeBroadcast(topic);
    }

    public void unsubscribeBroadcast(String topic) {
        pushHandlers.remove(topic);
        config.getRemoteClient().unsubscribeBroadcast(topic);
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
        config.getRemoteClient().setPushId(pushId);
    }

    public void onResponse(String path, int sequenceId, int code, String message, byte[] data) {
        ReplyHandler replyHandler = replayHandlers.remove(sequenceId);
        if (replyHandler != null) {
            if (code == 1) {
                try {
                    replyHandler.onSuccess(config.getRequestSerializer().toObject(path, replyHandler.clazz, data));
                } catch (RequestException e) {
                    replyHandler.onError(e.getCode(), e.getMessage());
                } catch (Exception e) {
                    replyHandler.onError(RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.value, RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.name());
                }
            } else {
                replyHandler.onError(code, message);
            }
        }
    }
}
