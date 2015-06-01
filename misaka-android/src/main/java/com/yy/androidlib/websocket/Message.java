package com.yy.androidlib.websocket;

import android.util.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private String command;
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;

    public static Message parse(String data) {
        data = data.replace("\\n", "\n");
        data = data.replace("\\u0000", "\000");
        data = data.replace("\\", "");
        Message message = new Message();
        try {

            BufferedReader reader = new BufferedReader(new StringReader(data));
            message.command = reader.readLine();
            String header;
            while ((header = reader.readLine()).length() > 0) {
                int ind = header.indexOf(':');
                String k = header.substring(0, ind);
                String v = header.substring(ind + 1, header.length());
                message.headers.put(k.trim(), v.trim());
            }
            StringBuffer body = new StringBuffer();
            int b;
            while ((b = reader.read()) != 0) {
                body.append((char) b);
            }
            message.body = body.toString();

        } catch (IOException e) {
            Log.e("STOMP", "parse message fail", e);
        }
        return message;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDestination() {
        return getHeaders().get("destination");
    }

    public static String toRawString(Command c, String[] h, String b) {
        StringBuffer message = new StringBuffer(c.toString());
        message.append("\n");

        if (h != null) {
            for (int i = 0; i + 1 < h.length; i = i + 2) {
                message.append(h[i]);
                message.append(":");
                message.append(h[i + 1]);
                message.append("\n");
            }
        }

        message.append("\n");


        if (b != null) message.append(b);

        message.append("\000");

        String data = "[" + JSONObject.quote(message.toString()) + "]";

        return data;
    }

}
