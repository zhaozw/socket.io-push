package com.yy.misaka.demo.appmodel;

import android.app.Application;
import android.os.Environment;
import com.yy.androidlib.util.logging.Logger;
import com.yy.androidlib.util.logging.YYAppender;
import com.yy.androidlib.util.logging.YYAppender.LogOptions;
import com.yy.androidlib.util.notification.NotificationCenter;
import com.yy.androidlib.websocket.Callback;
import com.yy.androidlib.websocket.Config;
import com.yy.androidlib.websocket.StompClient;
import com.yy.androidlib.websocket.YYHostResolver;
import com.yy.androidlib.websocket.login.Login;
import com.yy.misaka.demo.appmodel.callback.ImCallback;
import com.yy.misaka.demo.appmodel.callback.ProfileCallback;

import java.io.File;

/**
 * Created by xuduo on 3/4/15.
 */
public enum AppModel {
    INSTANCE;

//    private String host = "http://" + "dev.yypm.com";
    //    private String host = "http://" + "patch.3g.yy.com";
//    private String host = "http://" + "test.misaka.yy.com";
//    private String host = "http://" + "172.19.206.211";
    private String host = "http://" + "mlbs.yy.com";
    private StompClient stomp;
    private Login login;
    private ImModel im;
    private ProfileModel pm;

    public void init(Application application) {
        initLogging();
        stomp = new StompClient(application, host + ":8080", new Config().mode(Config.Mode.REMOTE).hostResolver(new YYHostResolver(application)).dataAsBody(false));
        stomp.addRoute("demo-server", host + ":8080");
        stomp.addRoute("1001","http://finance.yyembed.yy.com/1.0");
        stomp.addRoute("login", "http://uaas.yy.com");
        stomp.addConnectionCallback(new Callback() {
            @Override
            public void onConnected() {
                im.onConnected();
            }
        });
        login = new Login(application, stomp, "fm141027");
        im = new ImModel(application, stomp, login);
        pm = new ProfileModel(application, stomp, host + ":8091");
        initCallbacks();

//        AsyncHttpClient.getDefaultInstance().executeString(new AsyncHttpGet(host + ":8090/"), new AsyncHttpClient.StringCallback() {
//                    // Callback is invoked with any exceptions/errors, and the result, if available.
//                    @Override
//                    public void onCompleted(Exception e, AsyncHttpResponse response, String result) {
//                        if (e != null) {
//                            e.printStackTrace();
//                            return;
//                        }
//                        System.out.println("I got a string: " + result);
//                    }
//                });
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

    public StompClient getStomp() {
        return stomp;
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
