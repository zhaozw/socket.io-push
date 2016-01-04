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
    private Map<String, ReplyHandler> replayHandlers = new HashMap<>();

    public ProxyClient(Config config) {
        this.config = config;
        if (config.getRemoteClient() != null) {
            config.getRemoteClient().setProxyClient(this);
        }
    }

    public boolean isConnected(){
        return config.getRemoteClient().isConnected();
    }

    public void request(String path, Object body, final ReplyHandler replyHandler) {
        final RequestInfo requestInfo = new RequestInfo();
        requestInfo.setBody(config.getRequestSerializer().toBinary(path, body));
        requestInfo.setPath(path);

        if (replyHandler != null) {
            requestInfo.setExpectReply(true);
            replayHandlers.put(requestInfo.getSequenceId(), replyHandler);
        }

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
            if(data == null || data.length == 0 || config.getPushSerializer() == null) {
                result = null;
            } else {
                try {
                    result = config.getPushSerializer().toObject(topic, handler.clazz, data);
                } catch (Exception e) {
                    Log.e(TAG, "onPush serialize exception " + e.getMessage(), e);
                    return;
                }
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

    public void onResponse(String path, String sequenceId, int code, String message, byte[] data) {
        ReplyHandler replyHandler = replayHandlers.remove(sequenceId);
        if (replyHandler != null) {
            if (code == 1) {
                try {
                    callSuccessOnMainThread(replyHandler, config.getRequestSerializer().toObject(path, replyHandler.clazz, data));
                } catch (RequestException e) {
                    callErrorOnMainThread(replyHandler, new RequestException(e, e.getCode(), e.getMessage()));
                } catch (Exception e) {
                    callErrorOnMainThread(replyHandler, new RequestException(e, RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.value, RequestException.Error.CLIENT_DATA_SERIALIZE_ERROR.name()));
                }
            } else {
                callErrorOnMainThread(replyHandler, new RequestException(null, code, message));
            }
        }
    }

    public Config getConfig() {
        return config;
    }
}
