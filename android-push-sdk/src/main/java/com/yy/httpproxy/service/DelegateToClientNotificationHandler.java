package com.yy.httpproxy.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by xuduo on 11/6/15.
 */

public class DelegateToClientNotificationHandler extends DefaultNotificationHandler {

    @Override
    public void handlerNotification(Context context, boolean binded, PushedNotification pushedNotification) {

        if (!binded) {
            showNotification(context, pushedNotification);
        }

        sendArrived(context, pushedNotification);

    }

}
