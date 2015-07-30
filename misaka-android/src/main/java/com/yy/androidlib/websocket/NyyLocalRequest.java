package com.yy.androidlib.websocket;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.yy.androidlib.util.http.AsyncHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by xuduo on 7/21/15.
 */
public class NyyLocalRequest {

    private Gson gson = new Gson();

    public void request(String url, String data, String[] headerMap, final ReplyHandler replyHandler) {

        RequestParams params = new RequestParams();
        try {
            params.add("data", URLEncoder.encode(data,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            
        }
        if (headerMap != null && headerMap.length % 2 == 0) {
            for (int i = 0; i + 1 < headerMap.length; i = i + 2) {
                params.add(headerMap[i], headerMap[i + 1]);
            }
        }

        url = url + "?" + params.toString();

        AsyncHttp.get(url, new AsyncHttp.ResultCallback() {
            @Override
            public void onSuccess(String url, int statusCode, String result) {
                try {
                    JSONObject jObject = new JSONObject(result);
                    String data = jObject.optString("data");
                    replyHandler.handle(gson, data, 1, "success", "服务器数据解析错误!");
                } catch (JSONException e) {
                    replyHandler.handle(gson, "", -1, "error", "服务器数据解析错误!");
                }
            }

            @Override
            public void onFailure(String url, int statusCode, int errorType, Throwable throwable) {
                replyHandler.handle(gson, "", -1, "无法连接服务器", "无法连接服务器!");
            }
        });

    }
}
