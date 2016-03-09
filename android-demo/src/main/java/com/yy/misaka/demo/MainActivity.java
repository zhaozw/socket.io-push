package com.yy.misaka.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

/**
 * Created by Administrator on 2016/3/9.
 */
public class MainActivity extends Activity{

    public EditText editText;
    public final static String IPNAME = "ipAddress";
    public String defaultIp = "http://172.26.66.23:10001";
    public SharedPreferences mySharedPreferences;
    public TextView pushId;
    private String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText)findViewById(R.id.et_ip);
        pushId = (TextView) findViewById(R.id.tv_pushId);
        mySharedPreferences=getSharedPreferences(IPNAME, Activity.MODE_PRIVATE);
        initEditText();

        id = new SharedPreferencePushIdGenerator(this).generatePushId();
        pushId.setText("pushId: " + id);

        findViewById(R.id.bt_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = editText.getText().toString();
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString("ip", ip);
                editor.commit();

                Intent intent = new Intent();
                intent.putExtra("ipAddress", ip);
                intent.setClass(MainActivity.this, DrawActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initEditText(){
        String ip = mySharedPreferences.getString("ip", "0");
        if(ip.equals("0")){
            editText.setText(defaultIp);
        }else{
            editText.setText(ip);
        }
    }
}
