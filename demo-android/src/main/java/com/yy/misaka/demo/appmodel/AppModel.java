//package com.yy.misaka.demo.appmodel;
//
//import android.app.Application;
//import android.os.Environment;
//import android.util.Log;
//
//import com.google.gson.JsonElement;
//import com.yy.androidlib.util.logging.Logger;
//import com.yy.androidlib.util.logging.YYAppender;
//import com.yy.androidlib.util.logging.YYAppender.LogOptions;
//import com.yy.androidlib.websocket.Callback;
//import com.yy.androidlib.websocket.Config;
//import com.yy.androidlib.websocket.MisakaClient;
//import com.yy.androidlib.websocket.NyyLocalRequest;
//import com.yy.androidlib.websocket.ReplyHandler;
//import com.yy.androidlib.websocket.login.Login;
//import com.yy.misaka.demo.util.ExceptionHandler;
//
//import java.io.File;
//
///**
// * Created by xuduo on 3/4/15.
// */
//public enum AppModel {
//    INSTANCE;
//
////    private String lbsHost = "http://" + "183.61.6.33:8080"; //lbs服务器地址
////    private String lbsHost = "http://" + "172.19.103.101:8080"; //lbs服务器地址
//    private String lbsHost = "http://" + "172.19.12.176:8080"; //lbs服务器地址
//    //        private String lbsHost = "http://" + "172.19.207.244:8080"; //lbs服务器地址
//    private String demoHost = "http://" + "dev.yypm.com:8091"; //demo服务器地址
//    //        private String demoHost = "http://" + "172.19.207.244:8091"; //demo服务器地址
//    private MisakaClient misaka;
//    private Login login;
//    private ImModel im;
//    private ProfileModel pm;
//
//
//    static class Test {
//        public int lastOrderId = 0;
//        public int limit = 7;
//        public String auth = "no";
//        public int uid = 1000008;
//
//    }
//
//    public void init(Application application) {
//        initLogging();
//        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
//
////        misaka = new MisakaClient(application, lbsHost, new Config().useNyy(true));
////
////        misaka.request("http://172.19.12.25:8096", "/video/recommend/list", new String[]{"appId", "111"}, new Test(), new ReplyHandler<JsonElement>(JsonElement.class) {
////            @Override
////            public void onSuccess(JsonElement result) {
////                Log.i("123", result + "");//172.19.12.25:8096/video/recommend/list?appId=111&data={%22auth%22:%22no%22,%22lastOrderId%22:0,%22limit%22:7,%22uid%22:100000400}
////            }
////
////            @Override
////            public void onError(int code, String message) {
////                Log.i("123", message + "");
////            }
////
////        });
////        misaka.addConnectionCallback(new Callback() {
////            @Override
////            public void onConnected() {
////                im.onConnected();
////            }
////        });
////        login = new Login(application, misaka, "fm141027", "http://uaas.yy.com");
//        im = new ImModel(application, misaka, login);
//        pm = new ProfileModel(application, misaka, demoHost);
//    }
//
//    private void initLogging() {
//        YYAppender.LogOptions options = new YYAppender.LogOptions();
//        options.logFileName = "log.txt";
//        options.logLevel = LogOptions.LEVEL_VERBOSE;
//        YYAppender logWriter = new YYAppender(getLogDir(), options);
//        logWriter.setUniformTag("MyLogTag");
//        logWriter.setCallerStackTraceIndex(7);
//        Logger.init(logWriter);
//    }
//
//    private String getLogDir() {
//        return Environment.getExternalStorageDirectory().getPath() + File.separator
//                + "stomp" + File.separator + "log";
//    }
//
//    public MisakaClient getMisaka() {
//        return misaka;
//    }
//
//    public Login getLogin() {
//        return login;
//    }
//
//    public ImModel getIm() {
//        return im;
//    }
//
//    public ProfileModel getProfile() {
//        return pm;
//    }
//}
