package com.yy.misaka.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.yy.androidlib.websocket.*;
import com.yy.androidlib.websocket.login.RegisterRequest;
import com.yy.misaka.demo.appmodel.AppModel;


public class RegisterActivity extends Activity {

    private static final String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.btn_get_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = ((TextView) findViewById(R.id.et_phone)).getText().toString();

                AppModel.INSTANCE.getLogin().getRegisterSmsCode(phone, "86", new ReplyHandler<RegisterRequest>(RegisterRequest.class) {
                    @Override
                    public void onSuccess(RegisterRequest result) {
                        Toast.makeText(RegisterActivity.this, "get register code success ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(RegisterActivity.this, "get register code error " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        findViewById(R.id.btn_set_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = ((TextView) findViewById(R.id.et_phone)).getText().toString();
                String password = ((TextView) findViewById(R.id.et_password)).getText().toString();
                String code = ((TextView) findViewById(R.id.et_code)).getText().toString();

                AppModel.INSTANCE.getLogin().register(phone, password, code, new ReplyHandler<RegisterRequest>(RegisterRequest.class) {
                    @Override
                    public void onSuccess(RegisterRequest result) {
                        Toast.makeText(RegisterActivity.this, "setPassword success ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(int code, String message) {
                        Toast.makeText(RegisterActivity.this, "setPassword error " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
