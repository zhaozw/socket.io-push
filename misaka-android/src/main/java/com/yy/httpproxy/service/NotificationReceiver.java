package com.yy.httpproxy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

/**
 * Created by xuduo on 11/6/15.
 */
public abstract class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(RemoteService.INTENT) && intent.getIntExtra("cmd", -1) == RemoteService.CMD_NOTIFICATION_CLICKED) {
            String id = intent.getStringExtra("id");
            HashMap<String, Object> values = (HashMap<String, Object>) intent.getSerializableExtra("values");
            onNotificationClicked(context, id, values);
        }
    }

    public abstract void onNotificationClicked(Context context, String id, HashMap<String, Object> values);
}
