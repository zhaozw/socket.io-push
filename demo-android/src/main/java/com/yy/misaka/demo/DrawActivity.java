package com.yy.misaka.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.yy.httpproxy.Config;
import com.yy.httpproxy.ProxyClient;
import com.yy.httpproxy.PushHandler;
import com.yy.httpproxy.ReplyHandler;
import com.yy.httpproxy.nyy.NyySerializer;
import com.yy.httpproxy.serializer.StringPushSerializer;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.socketio.SocketIOProxyClient;
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import java.util.HashMap;
import java.util.Map;


public class DrawActivity extends Activity {

    private static final String TAG = "Demo";
    private DrawView drawView;
    private ProxyClient proxyClient;


    public static class Test {
        public int uid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                proxyClient.request("POST", "http", "google.com", 8080, "/clear", new HashMap<String, String>(), null, null);
            }
        });

        drawView = (DrawView) findViewById(R.id.draw_view);

        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DrawView.Dot dot = new DrawView.Dot();
                dot.xPercent = motionEvent.getX() / view.getWidth();
                dot.yPercent = motionEvent.getY() / view.getHeight();
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
//                    drawView.addDot(dot);

                    Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Content-Type", "application/x-www-form-urlencoded");
//                Test test = new Test();
//                test.uid = 1000008;
//                NyyRequestData data = new NyyRequestData();
//                data.setAppId("100001");
//                data.setData(test);
                    proxyClient.request("POST", "http", "google.com", 8080, "/addDot", headers, dot, null);

                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    drawView.endLine();

                    proxyClient.request("POST", "http", "google.com", 8080, "/endLine", new HashMap<String, String>(), dot, null);
                    return true;
                } else {
                    return false;
                }
            }
        });

        SocketIOProxyClient requetClient = new SocketIOProxyClient("http://172.19.207.65:9101");

        String host = "http://172.19.207.65:9101";
//        String host = "http://183.61.6.33:80";

        proxyClient = new ProxyClient(new Config().setRequestSerializer(new JsonSerializer()).setRequester(requetClient).setPushSubscriber(new RemoteClient(this, host, null)).setPushSerializer(new JsonPushSerializer()));

        proxyClient.subscribe("/addDot", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {

            @Override
            public void onSuccess(DrawView.Dot result) {
//                Toast toast = Toast.makeText(DrawActivity.this, "push test recived " + result, Toast.LENGTH_SHORT);
//                toast.show();
                drawView.addDot(result);
            }
        });

        proxyClient.subscribe("/clear", new PushHandler(null) {

            @Override
            public void onSuccess(Object result) {
                drawView.clear();
            }
        });

        proxyClient.subscribe("/endLine", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {

            @Override
            public void onSuccess(DrawView.Dot result) {
//                Toast toast = Toast.makeText(DrawActivity.this, "push test recived " + result, Toast.LENGTH_SHORT);
//                toast.show();
                drawView.endLine();
            }
        });

        proxyClient.setPushId(new SharedPreferencePushIdGenerator(this).generatePushId());

        proxyClient.subscribeBroadcast("/topic/pushAll", new PushHandler<String>(String.class) {

            @Override
            public void onSuccess(String result) {
                Toast toast = Toast.makeText(DrawActivity.this, "pushAll recived " + result, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
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


    }


}
