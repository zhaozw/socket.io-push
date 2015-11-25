//package com.yy.misaka.demo;
//
//import android.app.Activity;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.os.Bundle;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.yy.httpproxy.Config;
//import com.yy.httpproxy.ProxyClient;
//import com.yy.httpproxy.PushHandler;
//import com.yy.httpproxy.ReplyHandler;
//import com.yy.httpproxy.nyy.NyyRequestData;
//import com.yy.httpproxy.nyy.NyySerializer;
//import com.yy.httpproxy.serializer.StringPushSerializer;
//import com.yy.httpproxy.service.RemoteService;
//import com.yy.httpproxy.socketio.RemoteClient;
//import com.yy.httpproxy.socketio.SocketIOProxyClient;
//import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.LogManager;
//import java.util.logging.LogRecord;
//import java.util.logging.Logger;
//
//
//public class LoginActivity extends Activity {
//
//    private static final String TAG = "Demo";
//    private static final String PREFERENCE_NAME = "DEMO";
//    private static final String KEY_USERNAME = "USERNAME";
//    private static final String KEY_PASSWORD = "PASSWORD";
//
//    private ProxyClient proxyClient;
//
//    public static class AndroidLoggingHandler extends Handler {
//
//        public static void reset(Handler rootHandler) {
//            Logger rootLogger = LogManager.getLogManager().getLogger("");
//            Handler[] handlers = rootLogger.getHandlers();
//            for (Handler handler : handlers) {
//                rootLogger.removeHandler(handler);
//            }
//            LogManager.getLogManager().getLogger("").addHandler(rootHandler);
//        }
//
//        @Override
//        public void close() {
//        }
//
//        @Override
//        public void flush() {
//        }
//
//        @Override
//        public void publish(LogRecord record) {
//            if (!super.isLoggable(record))
//                return;
//
//            String name = record.getLoggerName();
//            int maxLength = 30;
//            String tag = name.length() > maxLength ? name.substring(name.length() - maxLength) : name;
//
//            try {
//                int level = getAndroidLevel(record.getLevel());
//                Log.println(level, tag, record.getMessage());
//                if (record.getThrown() != null) {
//                    Log.println(level, tag, Log.getStackTraceString(record.getThrown()));
//                }
//            } catch (RuntimeException e) {
//                Log.e("AndroidLoggingHandler", "Error logging message.", e);
//            }
//        }
//
//        static int getAndroidLevel(Level level) {
//            int value = level.intValue();
//            if (value >= 1000) {
//                return Log.ERROR;
//            } else if (value >= 900) {
//                return Log.WARN;
//            } else if (value >= 800) {
//                return Log.INFO;
//            } else {
//                return Log.DEBUG;
//            }
//        }
//    }
//
//    public static class Test {
//        public int uid;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        final EditText usernameEdit = (EditText) findViewById(R.id.et_username);
//        usernameEdit.setText(getHistoryUsername());
//        final EditText passwordEdit = (EditText) findViewById(R.id.et_password);
//        passwordEdit.setText(getHistoryPassword());
//
////        proxyClient = new ProxyClient(getApplicationContext(), "http://172.19.12.176:8080", new Config());
//
//        SocketIOProxyClient requetClient = new SocketIOProxyClient("http://172.19.207.65:9101");
//
//        String host = "http://172.19.207.65:9101";
////        String host = "http://183.61.6.33:80";
//
//        proxyClient = new ProxyClient(new Config().setRequestSerializer(new NyySerializer()).setRequester(requetClient).setPushSubscriber(new RemoteClient(this,host,null)).setPushSerializer(new StringPushSerializer()));
//
//        proxyClient.subscribe("/topic/test", new PushHandler<String>(String.class) {
//
//            @Override
//            public void onSuccess(String result) {
//                Toast toast = Toast.makeText(LoginActivity.this, "push test recived " + result, Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        });
//
//        proxyClient.setPushId(new SharedPreferencePushIdGenerator(this).generatePushId());
//
//        proxyClient.subscribeBroadcast("/topic/pushAll", new PushHandler<String>(String.class) {
//
//            @Override
//            public void onSuccess(String result) {
//                Toast toast = Toast.makeText(LoginActivity.this, "pushAll recived " + result, Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        });
//        findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/x-www-form-urlencoded");
//                Test test = new Test();
//                test.uid = 1000008;
//                NyyRequestData data = new NyyRequestData();
//                data.setAppId("100001");
//                data.setData(test);
//                proxyClient.request("POST", "http", "google.com", 8080, "/echo2", headers, data, new ReplyHandler<Test>(Test.class) {
//                    @Override
//                    public void onSuccess(Test result) {
//                        Log.d(TAG, "success " + result.uid);
//                    }
//
//                    @Override
//                    public void onError(int code, String message) {
//                        Log.d(TAG, "error " + message);
//                    }
//                });
//
//            }
//        });
//
////        findViewById(R.id.btn_to_register).setOnClickListener(new OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
////                startActivity(intent);
////            }
////        });
//
//    }
//
//    static class TeacherData {
//        public int actualEndIndex;
//    }
//
//    static class PageRequest {
//        public int startIndex;
//        public int count = 5;
//    }
//
//    private void saveAccount(String username, String password) {
//        Editor editor = getSharedPreferences().edit();
//        editor.putString(KEY_USERNAME, username);
//        editor.putString(KEY_PASSWORD, password);
//        editor.apply();
//    }
//
//    private String getHistoryUsername() {
//        return getSharedPreferences().getString(KEY_USERNAME, "18680268780");
//    }
//
//    private String getHistoryPassword() {
//        return getSharedPreferences().getString(KEY_PASSWORD, "qwe123456");
//    }
//
//    private SharedPreferences getSharedPreferences() {
//        return getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
//    }
//
//
//}
