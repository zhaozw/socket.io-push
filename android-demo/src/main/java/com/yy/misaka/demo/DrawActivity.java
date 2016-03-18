package com.yy.misaka.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.yy.httpproxy.Config;
import com.yy.httpproxy.ProxyClient;
import com.yy.httpproxy.PushHandler;
import com.yy.httpproxy.ReplyHandler;
import com.yy.httpproxy.serializer.JsonPushSerializer;
import com.yy.httpproxy.serializer.JsonSerializer;
import com.yy.httpproxy.subscribe.ConnectCallback;
import java.util.Random;


public class DrawActivity extends Activity implements ConnectCallback {

    private static final String TAG = "DrawActivity";
    private DrawView drawView;
    private ProxyClient proxyClient;
    private TextView latency;
    private TextView count;
    private TextView connect;
    private long totalTime;
    private long totalCount;
    public int myColors[] = {Color.BLACK, Color.DKGRAY, Color.CYAN, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA};
    public int myColor;
    private TextView msg;


    private void updateLatency(long timestamp) {
        totalTime += System.currentTimeMillis() - timestamp;
        totalCount++;
        latency.setText((totalTime / totalCount) + "ms");
    }

    private void update(long timestamp, int num) {
        totalTime += System.currentTimeMillis() - timestamp;
        latency.setText((totalTime / num) + "ms");
        count.setText("" + num);
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
        connect = (TextView) findViewById(R.id.tv_connect);
        msg = (TextView) findViewById(R.id.tv_msg);

        String pushServerHost = getIntent().getStringExtra("ipAddress");
//        String pushServerHost = "https://183.60.221.91:443";
//        String pushServerHost = "http://ws.tt.yy.csom";
//        String pushServerHost = "http://61.160.36.69:9101";
//        String pushServerHost = "http://patch.3g.yy.com:9101";
//        String pushServerHost = "http://183.60.221.91:9101";

        myColor = myColors[new Random().nextInt(myColors.length)];

        proxyClient = new ProxyClient(new Config(this.getApplicationContext())
                .setHost(pushServerHost)
                .setPushSerializer(new JsonPushSerializer())
                .setRequestSerializer(new JsonSerializer()).setConnectCallback(this));

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetLatency();
                proxyClient.request("/clear", null, null);
            }
        });

        drawView = (DrawView) findViewById(R.id.draw_view);

        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                DrawView.Dot dot = new DrawView.Dot();
                dot.xPercent = motionEvent.getX() / view.getWidth();
                dot.yPercent = motionEvent.getY() / view.getHeight();
                dot.myColor = myColor;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                    proxyClient.request("/addDot", dot, new ReplyHandler<DrawView.Dot>(DrawView.Dot.class) {
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
                    proxyClient.request("/endLine", dot, null);
                    return true;
                } else {
                    return false;
                }
            }
        });


        proxyClient.subscribeBroadcast("/addDot", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {
            @Override
            public void onSuccess(DrawView.Dot result) {
                drawView.addDot(result);
                count.setText(totalCount + "dots");
            }
        });

        proxyClient.subscribeBroadcast("/clear", new PushHandler(null) {

            @Override
            public void onSuccess(Object result) {
                drawView.clear();
                resetLatency();
            }

        });

        proxyClient.subscribeAndReceiveTtlPackets("message", new PushHandler<Message>(Message.class) {
            @Override
            public void onSuccess(Message result) {
                msg.setText("Msg: " + result.message);
            }
        });

        proxyClient.subscribeBroadcast("/endLine", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {
            @Override
            public void onSuccess(DrawView.Dot result) {
                drawView.endLine();
            }
        });

        updateConnect();
    }

    private void updateConnect() {
        connect.setText(proxyClient.isConnected() ? "connected" : "disconnected");
    }

    @Override
    public void onConnect(String uid) {
        updateConnect();
    }

    @Override
    public void onDisconnect() {
        updateConnect();
    }
}