package com.yy.misaka.demo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.yy.httpproxy.service.NotificationReceiver;

import java.util.HashMap;

/**
 * Created by xuduo on 11/6/15.
 */
public class YYNotificationReceiver extends NotificationReceiver {
    @Override
    public void onNotificationClicked(Context context, String id, HashMap<String, Object> values) {
        Log.d("YYNotificationReceiver", "onNotificationClicked " + id + " values " + values);
        Toast.makeText(context, "YYNotificationReceiver clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, DrawActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
