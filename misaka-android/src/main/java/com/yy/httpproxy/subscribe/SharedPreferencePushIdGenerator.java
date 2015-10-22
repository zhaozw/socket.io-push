package com.yy.httpproxy.subscribe;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by xuduo on 10/20/15.
 */
public class SharedPreferencePushIdGenerator implements PushIdGenerator {

    private Context context;

    public SharedPreferencePushIdGenerator(Context context) {
        this.context = context;
    }

    public String nextSessionId() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    @Override
    public String generatePushId() {
        SharedPreferences preferences = context.getSharedPreferences("SharedPreferencePushGenerator", Context.MODE_PRIVATE);
        String id = preferences.getString("pushId", null);
        if (id == null) {
            id = nextSessionId();
            preferences.edit().putString("pushId", id).commit();
        }
        return id;
    }
}
