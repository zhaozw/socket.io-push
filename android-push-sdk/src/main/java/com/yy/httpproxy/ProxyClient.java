package com.yy.httpproxy;


import android.os.Handler;
import android.os.Looper;

import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.requester.RequestInfo;
import com.yy.httpproxy.subscribe.PushCallback;

import java.util.HashMap;
import java.util.Map;

public class ProxyClient implements PushCallback {

    private Config config;
    public static final String TAG = "ProxyClient";
    private long mainThreadId = Looper.getMainLooper().getThread().getId();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, ReplyHandler> replayHandlers = new HashMap<>();

    public ProxyClient(Config config) {
        this.config = config;
        if (config.getRemoteClient() != null) {
            config.getRemoteClient().setProxyClient(this);
        }
    }

    public boolean isConnected() {
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

    public void reportStats(String path, int successCount, int errorCount, int latency) {
        config.getRemoteClient().
                reportStats(path, successCount, errorCount, latency);
    }

    private void subscribeBroadcast(String topic, boolean receiveTtlPackets) {
        config.getRemoteClient().subscribeBroadcast(topic, receiveTtlPackets);
    }

    public void subscribeBroadcast(String topic) {
        config.getRemoteClient().subscribeBroadcast(topic, false);
    }

    public void subscribeAndReceiveTtlPackets(String topic) {
        config.getRemoteClient().subscribeBroadcast(topic, true);
    }

    public void unsubscribeBroadcast(String topic) {
        config.getRemoteClient().unsubscribeBroadcast(topic);
    }

    @Override
    public void onPush(final String topic, final byte[] data) {
        if (config.getPushCallback() != null) {
            if (Thread.currentThread().getId() == mainThreadId) {
                config.getPushCallback().onPush(topic, data);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        config.getPushCallback().onPush(topic, data);
                    }
                });
            }
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


    public void getPushId() {
        getConfig().getPushId();
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
