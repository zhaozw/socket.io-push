package com.yy.androidlib.websocket.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.util.Map;

public class NetworkMonitor {

    private static BroadcastReceiver networkStateReceiver;
    private static Map<NetworkChanged, Object> callbacks;
    private static boolean connected;

    public interface NetworkChanged {

        void onNetworkConnect();

        void onNetworkDisconnect();
    }

    public synchronized static void addMonitor(Context context, NetworkChanged callback) {
        if (callbacks == null) {
            callbacks = new HashMap<NetworkChanged, Object>();
        }
        callbacks.put(callback, true);

        if (networkStateReceiver == null) {
            context = context.getApplicationContext();
            connected = isConnected(context);
            networkStateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    doBroadcast(context);
                }
            };
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(networkStateReceiver, filter);
        }

        notifyConnect(callback);
    }

    public synchronized static void removeMonitor(NetworkChanged callback) {
        if (callbacks != null) {
            callbacks.remove(callback);
        }
    }


    private static void notifyConnect(NetworkChanged callback) {
        if (connected) {
            callback.onNetworkConnect();
        } else {
            callback.onNetworkDisconnect();
        }
    }

    private static void doBroadcast(Context context) {
        boolean newConnected = isConnected(context);
        if (newConnected != connected) {
            connected = newConnected;
            for (NetworkChanged callback : callbacks.keySet()) {
                notifyConnect(callback);
            }
        }

    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
