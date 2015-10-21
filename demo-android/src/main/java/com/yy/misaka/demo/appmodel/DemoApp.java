package com.yy.misaka.demo.appmodel;

import android.app.Application;

/**
 * Created by xuduo on 3/4/15.
 */
public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
       // AppModel.INSTANCE.init(this);
//        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).build();
//        ImageLoader.getInstance().init(configuration);
    }
}
