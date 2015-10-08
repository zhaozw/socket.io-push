package com.yy.misaka.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.yy.androidlib.websocket.Destination;
import com.yy.androidlib.websocket.ReplyHandler;
import com.yy.androidlib.websocket.login.LoginRequest;
import com.yy.misaka.demo.appmodel.AppModel;


public class LoginActivity extends Activity {

    private static final String TAG = "Demo";
    private static final String PREFERENCE_NAME = "DEMO";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_PASSWORD = "PASSWORD";

    private Socket mSocket;
    try {
        IO.Options opts = new IO.Options();
        opts.transports = new String[]{"websocket"};
        mSocket = IO.socket(Constants.CHAT_SERVER_URL ,opts);
    } catch (URISyntaxException e) {
        throw new RuntimeException(e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEdit = (EditText) findViewById(R.id.et_username);
        usernameEdit.setText(getHistoryUsername());
        final EditText passwordEdit = (EditText) findViewById(R.id.et_password);
        passwordEdit.setText(getHistoryPassword());

        findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject object = new JSONObject();

                JSONObject headers = new JSONObject();
                headers.put("header1", "value1");
                headers.put("header2", "value2");
                object.put("headers", headers);

                JSONObject options = new JSONObject();
                options.put("method", "get");

                String params = "data=1234";
                object.put("body", params);

                object.put("url","http://mlbs.yy.com:8080/echo?data=1234");

                mSocket.emit("httpProxy", object);

                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                mTyping = false;

                String message = mInputMessageView.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    mInputMessageView.requestFocus();
                    return;
                }

                mInputMessageView.setText("");
                addMessage(mUsername, message);

                // perform the sending message attempt.
                mSocket.emit("new message", message);
            }
        });

        findViewById(R.id.btn_to_register).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("httpProxy", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Log.i("", "response" + data.optString("response"));
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
