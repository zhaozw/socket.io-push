package com.yy.misaka.demo.appmodel;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.yy.httpproxy.service.DefaultNotificationHandler;
import com.yy.httpproxy.service.NotificationReceiver;
import com.yy.httpproxy.service.PushedNotification;
import com.yy.misaka.demo.DrawActivity;

import java.util.HashMap;

/**
 * Created by xuduo on 11/6/15.
 */
public class YYNotificationReceiver extends NotificationReceiver {

    @Override
    public void onNotificationClicked(Context context, PushedNotification notification) {
        Log.d("YYNotificationReceiver", "onNotificationClicked " + notification.id + " values " + notification.values);
        Toast.makeText(context, "YYNotificationReceiver clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, DrawActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onNotificationArrived(Context context, PushedNotification notification) {
        Log.d("YYNotificationReceiver", "onNotificationArrived " + notification.id + " values " + notification.values);
    }

}
