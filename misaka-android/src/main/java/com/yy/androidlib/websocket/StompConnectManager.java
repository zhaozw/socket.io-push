package com.yy.androidlib.websocket;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.yy.androidlib.websocket.util.NetworkMonitor;

public class StompConnectManager implements WebSocketListener, NetworkMonitor.NetworkChanged {

    private Handler socketHandler;
    private Handler mainHandler;
    private IWebSocketClient webSocket;
    private Context context;
    private StompListener stompListener;
    private static final String TAG = "stomp";
    private int requestId = 1;
    private boolean subscribeConnected = false;
    private long heartbeatInterval = 1 * 60 * 1000;
    private long lastHeartbeat;

    private Runnable connectTask = new Runnable() {
        @Override
        public void run() {
            doConnect();
            checkHeartbeat();
            socketHandler.removeCallbacks(this);
            socketHandler.postDelayed(this, 3000L);
        }
    };

    public boolean isConnected() {
        return subscribeConnected;
    }

    public interface StompListener {

        void onConnected();

        void onDisconnected(String reason, Exception e);

        void onMessage(Message message);
    }

    private void doConnect() {
        if (webSocket != null && webSocket.isDisconnected() && NetworkMonitor.isConnected(context)) {
            Log.i(TAG, "StompConnectManager doConnect " + Thread.currentThread().getName());
            webSocket.connect();
        }
    }

    public StompConnectManager(Context context, Config config, Handler mainHandler, String url, StompListener stompListener) {
        HandlerThread mHandlerThread = new HandlerThread("websocket-thread");
        mHandlerThread.start();
        this.context = context;
        this.stompListener = stompListener;
        this.mainHandler = mainHandler;
        socketHandler = new Handler(mHandlerThread.getLooper());
        NetworkMonitor.addMonitor(context, this);
        webSocket = WebSocketFactory.local(url, this, config.getHostResolver());
        socketHandler.postDelayed(connectTask, 1000l);
    }

    @Override
    public void onNetworkConnect() {
        Log.i(TAG, "StompConnectManager onNetworkConnect");
        socketHandler.postDelayed(connectTask, 1000l);
    }

    @Override
    public void onNetworkDisconnect() {
        subscribeConnected = false;
        Log.i(TAG, "StompConnectManager onNetworkDisconnect");
        socketHandler.removeCallbacks(connectTask);
    }

    @Override
    public void onConnect() {
        sendCommand(Command.CONNECT, null);
    }

    @Override
    public void onMessage(String string) {
        final Message message = Message.parse(string);
        Log.d(TAG, "StompConnectManager onMessage \n" + message.getCommand() + "\nheader\n" + message.getHeaders() + "\nbody\n" + message.getBody());
        if (Command.CONNECTED.is(message.getCommand())) {
            Log.d(TAG, "connected stomp");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    subscribe("/user/queue/reply");
                }
            });
        } else if (Command.MESSAGE.is(message.getCommand())) {
            if ("/user/queue/reply".equals(message.getDestination()) && "CONNECTED".equals(message.getBody())) {
                Log.i(TAG, "StompConnectManager /user/queue/reply connected");
                subscribeConnected = true;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stompListener.onConnected();
                    }
                });
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stompListener.onMessage(message);
                    }
                });
            }
        }
    }

    @Override
    public void onDisconnect(final String reason, final Exception e) {
        Log.e(TAG, "StompConnectManager onDisconnect " + reason, e);
        subscribeConnected = false;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                stompListener.onDisconnected(reason, e);
            }
        });
    }

    @Override
    public void onHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }

    private void checkHeartbeat() {
        if (heartbeatInterval > 0 && !webSocket.isDisconnected() && lastHeartbeat > 0) {
            long sinceLastBeat = System.currentTimeMillis() - lastHeartbeat;
            if (sinceLastBeat > heartbeatInterval * 1.1) {
                Log.i(TAG, "heartbeat fail ,reconnect");
                reconnect();
            }
        }
    }

    public void reconnect() {
        subscribeConnected = false;
        webSocket.close();
    }


    public void subscribe(String path) {
        sendCommand(Command.SUBSCRIBE, null, Headers.DESTINATION_HEADER, path, Headers.SUBSCRIPTION_ID_HEADER, "sub_" + path);
    }

    public int send(String path, String body, String... headers) {
        if (!subscribeConnected) {
            return 0;
        }
        requestId = requestId + 1;
        final String key = String.valueOf(requestId);
        if (headers == null || headers.length == 0) {
            headers = new String[]{};
        }
        String[] newHeaders = new String[headers.length + 4];
        for (int i = 0; i < headers.length; i++) {
            newHeaders[i] = headers[i];
        }
        newHeaders[headers.length] = Headers.DESTINATION_HEADER;
        newHeaders[headers.length + 1] = path;
        newHeaders[headers.length + 2] = "request-id";
        newHeaders[headers.length + 3] = key;
        if (sendCommand(Command.SEND, body, newHeaders)) {
            return requestId;
        } else {
            return 0;
        }
    }

    private boolean sendCommand(Command command, String text, String... headers) {
        String transmit = Message.toRawString(command, headers, text);
        if (webSocket != null) {
            return webSocket.send(transmit);
        } else {
            return false;
        }
    }
}
