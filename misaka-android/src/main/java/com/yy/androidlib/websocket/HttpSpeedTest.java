package com.yy.androidlib.websocket;

import android.util.Log;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import org.apache.http.Header;

import java.util.List;

/**
 * Created by xuduo on 4/23/15.
 */
public class HttpSpeedTest {

    private static final String TAG = "HttpSpeedTest";
    private String result;
    private long timeout;
    private List<String> urls;
    private SyncHttpClient client = new SyncHttpClient();
    private Thread thread ;

    public HttpSpeedTest(List<String> urls, long timeout) {
        this.urls = urls;
        this.timeout = timeout;
        thread = Thread.currentThread();
    }

    public String speedTest(String def, final boolean returnUrl) {
        result = null;
        final long timestamp = System.currentTimeMillis();
//        FutureTask<>
//        List<Thread>
        for (final String url : urls) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.get(url, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Log.i(TAG, "HttpSpeedTest fail url " + url + " , " + (System.currentTimeMillis() - timestamp));
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            Log.i(TAG, "HttpSpeedTest code " + statusCode + " success url " + url + " , " + (System.currentTimeMillis() - timestamp));
                            if (statusCode == 200 && result == null) {
                                if (returnUrl) {
                                    result = url;
                                } else {
                                    result = responseString;
                                }
                                thread.interrupt();
                            }
                        }
                    });
                }
            }).start();

        }

        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            Log.v(TAG, "HttpTest done interrupted " + result);
        }
        if (result == null) {
            result = def;
        }
        Log.v(TAG, "HttpTest result " + def + " " + result);
        return result;
    }

}
