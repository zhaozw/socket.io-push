package com.yy.httpproxy;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.yy.androidlib.websocket.Config;
import com.yy.androidlib.websocket.ReplyHandler;
import com.yy.androidlib.websocket.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ProxyClient {

    private Map<Integer, Request> replyCallbacks = new HashMap<Integer, Request>();
    private Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer() {
        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : new Date(json.getAsLong());
        }
    }).registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? new JsonPrimitive(0l) : new JsonPrimitive(src.getTime());
        }
    }).create();

    private int sequenceId;

    private final Emitter.Listener httpProxyListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i("", "response" + data.optString("response"));
            int responseSeqId = data.optInt("sequenceId", 0);
            int responseCode = data.optInt("responseCode", 0);
            String responseMessage = data.optString("responseMessage", "no error message from server");
            String response = data.optString("response", "no response body from server");
            Request request = replyCallbacks.remove(responseSeqId);
            if (request != null && request.getReplyHandler() != null) {
                if (responseCode == 1) {
                    request.getReplyHandler().handle(gson, response, 1, "www", "qqq");
                } else {
                    request.getReplyHandler().onError(responseCode, responseMessage);
                }
            }
        }
    };


    private Socket mSocket;

    public ProxyClient(Context context, String host, Config config) {
        AndroidLoggingHandler.reset(new AndroidLoggingHandler());
        java.util.logging.Logger.getLogger("").setLevel(Level.FINEST);

        try {
            IO.Options opts = new IO.Options();
            opts.transports = new String[]{"websocket"};
            mSocket = IO.socket(host, opts);
            mSocket.on("httpProxy", httpProxyListener);
            mSocket.connect();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void request(String host, String destination, Object body, final ReplyHandler replyHandler) {
        String url = host + destination;
        request(url, body, null, replyHandler);
    }

    public void request(String host, String destination, String[] headerMap, Object body, final ReplyHandler replyHandler) {
        String url = host + destination;
        request(url, body, headerMap, replyHandler);
    }

    private void request(String url, Object body, String[] headerMap, final ReplyHandler replyHandler) {
        JSONObject object = new JSONObject();

        JSONObject headers = new JSONObject();
        try {


            sequenceId = sequenceId + 1;
            headers.put("header1", "value1");
            headers.put("header2", "value2");
            object.put("headers", headers);

            JSONObject options = new JSONObject();
            options.put("method", "get");

            object.put("body", body);

            object.put("url", url);

            object.put("sequenceId", String.valueOf(sequenceId));

            Request request = new Request(url, body, replyHandler);

            replyCallbacks.put(sequenceId, request);

            mSocket.emit("httpProxy", object);

            if (!mSocket.connected()) return;


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
