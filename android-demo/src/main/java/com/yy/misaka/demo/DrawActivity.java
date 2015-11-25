package com.yy.misaka.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yy.httpproxy.Config;
import com.yy.httpproxy.ProxyClient;
import com.yy.httpproxy.PushHandler;
import com.yy.httpproxy.ReplyHandler;
import com.yy.httpproxy.socketio.RemoteClient;
import com.yy.httpproxy.socketio.SocketIOProxyClient;
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import java.util.HashMap;
import java.util.Map;


public class DrawActivity extends Activity {

    private static final String TAG = "Demo";
    private DrawView drawView;
    private ProxyClient proxyClient;
    private TextView latency;
    private TextView count;
    private long totalTime;
    private long totalCount;


    private void updateLatency(long timestamp) {
        totalTime += System.currentTimeMillis() - timestamp;
        totalCount++;
        latency.setText((totalTime / totalCount) + "ms");
    }

    private void resetLatency() {
        totalCount = 0;
        totalTime = 0;
        latency.setText("0ms");
        count.setText("0dots");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        latency = (TextView) findViewById(R.id.tv_latency);
        count = (TextView) findViewById(R.id.tv_count);

        String pushServerHost = "http://183.61.6.33";

        RemoteClient client = new RemoteClient(this.getApplicationContext(), pushServerHost, null);
        proxyClient = new ProxyClient(new Config().setRequester(client).setPushSubscriber(client).setPushSerializer(new JsonPushSerializer()).setRequestSerializer(new JsonSerializer()));

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetLatency();
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

                    Map<String, String> headers = new HashMap<String, String>();

                    proxyClient.request("POST", "http", "google.com", 8080, "/addDot", headers, dot, new ReplyHandler<DrawView.Dot>(DrawView.Dot.class) {
                        @Override
                        public void onSuccess(DrawView.Dot result) {
                            Log.d(TAG, "proxy reply " + result);
                            updateLatency(result.timestamp);
                        }

                        @Override
                        public void onError(int code, String message) {

                        }
                    });

                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    proxyClient.request("POST", "http", "google.com", 8080, "/endLine", new HashMap<String, String>(), dot, null);
                    return true;
                } else {
                    return false;
                }
            }
        });


        proxyClient.subscribe("/addDot", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {
            @Override
            public void onSuccess(DrawView.Dot result) {
                drawView.addDot(result);
                count.setText(totalCount + "dots");
            }
        });

        proxyClient.subscribe("/clear", new PushHandler(null) {

            @Override
            public void onSuccess(Object result) {
                drawView.clear();
                resetLatency();
            }

        });

        proxyClient.subscribe("/endLine", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {

            @Override
            public void onSuccess(DrawView.Dot result) {
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


    }


}
