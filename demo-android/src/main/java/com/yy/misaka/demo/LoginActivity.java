package com.yy.misaka.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.yy.androidlib.websocket.Config;
import com.yy.androidlib.websocket.ReplyHandler;
import com.yy.httpproxy.ProxyClient;

import java.net.URISyntaxException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class LoginActivity extends Activity {

    private static final String TAG = "Demo";
    private static final String PREFERENCE_NAME = "DEMO";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";

    private ProxyClient proxyClient;

    public static class AndroidLoggingHandler extends Handler {

        public static void reset(Handler rootHandler) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            LogManager.getLogManager().getLogger("").addHandler(rootHandler);
        }

        @Override
        public void close() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void publish(LogRecord record) {
            if (!super.isLoggable(record))
                return;

            String name = record.getLoggerName();
            int maxLength = 30;
            String tag = name.length() > maxLength ? name.substring(name.length() - maxLength) : name;

            try {
                int level = getAndroidLevel(record.getLevel());
                Log.println(level, tag, record.getMessage());
                if (record.getThrown() != null) {
                    Log.println(level, tag, Log.getStackTraceString(record.getThrown()));
                }
            } catch (RuntimeException e) {
                Log.e("AndroidLoggingHandler", "Error logging message.", e);
            }
        }

        static int getAndroidLevel(Level level) {
            int value = level.intValue();
            if (value >= 1000) {
                return Log.ERROR;
            } else if (value >= 900) {
                return Log.WARN;
            } else if (value >= 800) {
                return Log.INFO;
            } else {
                return Log.DEBUG;
            }
        }
    }

    public static class Test {
        public String data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEdit = (EditText) findViewById(R.id.et_username);
        usernameEdit.setText(getHistoryUsername());
        final EditText passwordEdit = (EditText) findViewById(R.id.et_password);
        passwordEdit.setText(getHistoryPassword());

        proxyClient = new ProxyClient(getApplicationContext(), "http://183.61.6.33:8080", new Config());

        findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                proxyClient.request("http://mlbs.yy.com:8080", "/echo", new Test(), new ReplyHandler<Test>(Test.class) {
                    @Override
                    public void onSuccess(Test result) {
                        Log.d(TAG,"success " + result.data);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.d(TAG,"error " + message);
                    }
                });

            }
        });

        findViewById(R.id.btn_to_register).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    static class TeacherData {
        public int actualEndIndex;
    }

    static class PageRequest {
        public int startIndex;
        public int count = 5;
    }

    private void saveAccount(String username, String password) {
        Editor editor = getSharedPreferences().edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    private String getHistoryUsername() {
        return getSharedPreferences().getString(KEY_USERNAME, "18680268780");
    }

    private String getHistoryPassword() {
        return getSharedPreferences().getString(KEY_PASSWORD, "qwe123456");
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
    }


}
