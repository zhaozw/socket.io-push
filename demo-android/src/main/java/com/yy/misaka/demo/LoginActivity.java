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
                LoginRequest user = new LoginRequest();
                final String username = usernameEdit.getText().toString();
                final String password = passwordEdit.getText().toString();
                user.setPhone(username);
                user.setPassword(password);

//                User usr = new User();
//                usr.setNick("qqq");
//                usr.setPortrait("ppp");
//                AppModel.INSTANCE.getStomp().request("demo-server", "/echo", usr, new ReplyHandler<User>(User.class) {
//                    @Override
//                    public void onSuccess(User result) {
//                        Toast.makeText(LoginActivity.this, "request success " + result, Toast.LENGTH_LONG).show();
//                    }
//
//                    @Override
//                    public void onError(int code, String message) {
//                        Toast.makeText(LoginActivity.this, "request error!code:" + code + " ,message:" + message, Toast.LENGTH_LONG).show();
//                    }
//                });

                AppModel.INSTANCE.getStomp().request("1001", "/getGuruList", new PageRequest(), new ReplyHandler<TeacherData>(TeacherData.class) {
                    @Override
                    public void onSuccess(TeacherData result) {
                        Log.i("stomp", " result " + result.actualEndIndex);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.i("stomp", " error " + message);
                    }
                });

//                AppModel.INSTANCE.getLogin().login(user, new ReplyHandler<LoginRequest>(LoginRequest.class) {
//                    @Override
//                    public void onSuccess(LoginRequest result) {
//                        Log.i(TAG, "onSuccess " + result);
//                        Toast.makeText(LoginActivity.this, "login success uid :" +AppModel.INSTANCE.getLogin().getCurrentUid(), Toast.LENGTH_LONG).show();
//                        saveAccount(username, password);
//                        Intent intent = new Intent(LoginActivity.this, ImUserListActivity.class);
//                        startActivity(intent);
//                        AppModel.INSTANCE.getProfile().queryMyInfo();
//                    }
//
//                    @Override
//                    public void onError(int code, String message) {
//                        Toast.makeText(LoginActivity.this, "request error!code:" + code + " ,message:" + message, Toast.LENGTH_LONG).show();
//                    }
//                });
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
