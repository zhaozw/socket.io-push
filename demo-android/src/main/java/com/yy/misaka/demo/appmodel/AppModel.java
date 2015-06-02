package com.yy.misaka.demo.appmodel;

import android.app.Application;
import android.os.Environment;
import com.yy.androidlib.util.logging.Logger;
import com.yy.androidlib.util.logging.YYAppender;
import com.yy.androidlib.util.logging.YYAppender.LogOptions;
import com.yy.androidlib.util.notification.NotificationCenter;
import com.yy.androidlib.websocket.Callback;
import com.yy.androidlib.websocket.Config;
import com.yy.androidlib.websocket.MisakaClient;
import com.yy.androidlib.websocket.login.Login;
import com.yy.misaka.demo.appmodel.callback.ImCallback;
import com.yy.misaka.demo.appmodel.callback.ProfileCallback;

import java.io.File;

/**
 * Created by xuduo on 3/4/15.
 */
public enum AppModel {
    INSTANCE;

    private String lbsHost = "http://" + "dev.yypm.com:8080"; //lbs服务器地址
//        private String lbsHost = "http://" + "172.19.207.244:8080"; //lbs服务器地址
    private String demoHost = "http://" + "dev.yypm.com:8091"; //demo服务器地址
//        private String demoHost = "http://" + "172.19.207.244:8091"; //demo服务器地址
    private MisakaClient misaka;
    private Login login;
    private ImModel im;
    private ProfileModel pm;

    public void init(Application application) {
        initLogging();

        misaka = new MisakaClient(application, lbsHost, new Config().dataAsBody(true));
        misaka.addRoute("demo-server", demoHost);
        misaka.addRoute("login", "http://uaas.yy.com");
        misaka.addConnectionCallback(new Callback() {
            @Override
            public void onConnected() {
                im.onConnected();
            }
        });
        login = new Login(application, misaka, "fm141027");
        im = new ImModel(application, misaka, login);
        pm = new ProfileModel(application, misaka, demoHost);
        initCallbacks();
    }

    private void initLogging() {
        YYAppender.LogOptions options = new YYAppender.LogOptions();
        options.logFileName = "log.txt";
        options.logLevel = LogOptions.LEVEL_VERBOSE;
        YYAppender logWriter = new YYAppender(getLogDir(), options);
        logWriter.setUniformTag("MyLogTag");
        logWriter.setCallerStackTraceIndex(7);
        Logger.init(logWriter);
    }

    private String getLogDir() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator
                + "stomp" + File.separator + "log";
    }

    private void initCallbacks() {
        NotificationCenter.INSTANCE.addCallbacks(ImCallback.class);
        NotificationCenter.INSTANCE.addCallbacks(ProfileCallback.class);
    }

    public MisakaClient getMisaka() {
        return misaka;
    }

    public Login getLogin() {
        return login;
    }

    public ImModel getIm() {
        return im;
    }

    public ProfileModel getProfile() {
        return pm;
    }
}
