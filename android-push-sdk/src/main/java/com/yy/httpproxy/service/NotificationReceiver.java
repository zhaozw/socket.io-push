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
        if (intent.getIntExtra("cmd", -1) == BindService.CMD_NOTIFICATION_CLICKED) {
            String id = intent.getStringExtra("id");
            HashMap<String, Object> values = (HashMap<String, Object>) intent.getSerializableExtra("notification");
            PushedNotification notification = new PushedNotification(id, values);
            onNotificationClicked(context, notification);
        } else if (intent.getIntExtra("cmd", -1) == BindService.CMD_NOTIFICATION_ARRIVED) {
            String id = intent.getStringExtra("id");
            HashMap<String, Object> values = (HashMap<String, Object>) intent.getSerializableExtra("notification");
            PushedNotification notification = new PushedNotification(id, values);
            onNotificationArrived(context, notification);
        }
    }

    public abstract void onNotificationClicked(Context context, PushedNotification notification);

    public abstract void onNotificationArrived(Context context, PushedNotification notification);
}
