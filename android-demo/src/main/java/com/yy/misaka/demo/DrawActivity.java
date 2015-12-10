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
import com.yy.httpproxy.subscribe.SharedPreferencePushIdGenerator;

import java.util.Random;


public class DrawActivity extends Activity {

    private static final String TAG = "Demo";
    private DrawView drawView;
    private ProxyClient proxyClient;
    private TextView latency;
    private TextView count;
    private long totalTime;
    private long totalCount;
    public int myColors[] = {Color.BLACK, Color.DKGRAY, Color.CYAN, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA};
    public int myColor;


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

        String pushServerHost = "http://183.61.6.33:8080";
//        String pushServerHost = "http://113.107.236.239";
//        String pushServerHost = "http://61.147.186.58";
//        String pushServerHost = "http://172.25.133.154:9101";

        Random random = new Random();
        int num = random.nextInt(myColors.length);
        myColor = myColors[num];

        proxyClient = new ProxyClient(new Config(this.getApplicationContext())
                .setHost(pushServerHost)
                .setPushSerializer(new JsonPushSerializer())
                .setRequestSerializer(new JsonSerializer()));

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetLatency();
                proxyClient.request("/clear", null, null);
            }
        });

        findViewById(R.id.btn_clearColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawView.Dot dot = new DrawView.Dot();
                dot.myColor = myColor;
                proxyClient.request("/clearColor", dot, null);
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

        proxyClient.subscribeBroadcast("/endLine", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {
            @Override
            public void onSuccess(DrawView.Dot result) {
                drawView.endLine();
            }
        });

        proxyClient.subscribeBroadcast("/clearColor", new PushHandler<DrawView.Dot>(DrawView.Dot.class) {
            @Override
            public void onSuccess(DrawView.Dot result) {
                int count = drawView.clearColor(result);
                if (count == 0) {
                    resetLatency();
                } else {
                    update(result.timestamp, count);
                }
            }
        });

        proxyClient.setPushId(new SharedPreferencePushIdGenerator(this).generatePushId());

    }

}
