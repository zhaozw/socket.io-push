package com.yy.httpproxy.service;

import android.content.Context;

/**
 * Created by xuduo on 11/6/15.
 */
public interface NotificationHandler {

    void handlerNotification(Context context, boolean binded, PushedNotification notification);

}
