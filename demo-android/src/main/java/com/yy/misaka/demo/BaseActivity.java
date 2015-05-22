package com.yy.misaka.demo;

import android.app.Activity;
import android.os.Bundle;
import com.yy.androidlib.util.notification.NotificationCenter;

/**
 * User: lxl
 * Date: 3/4/15
 * Time: 5:38 PM
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter.INSTANCE.addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationCenter.INSTANCE.removeObserver(this);
    }
}
