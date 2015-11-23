package com.yy.misaka.demo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yy.httpproxy.nyy.NyyRequestData;
import com.yy.httpproxy.requester.RequestException;
import com.yy.httpproxy.serializer.RequestSerializer;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;

public class JsonSerializer implements RequestSerializer {

    private JsonParser parser = new JsonParser();
    private Gson gson = new Gson();

    @Override
    public byte[] toBinary(String path, Object body) {
        String data = gson.toJson(body);
        try {
            return data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @Override
    public Object toObject(Object clazz, int statusCode, Map<String, String> headers, byte[] body) throws RequestException {
        try {
            JsonObject root = (JsonObject) parser.parse(new String(body, "UTF-8"));
            int code = root.get("code").getAsInt();
            if (code != 1) {
                throw new RequestException(null, code, root.get("msg").getAsString());
            }
            JsonElement data = root.get("data");
            if (data == null) {
                return null;
            } else {
                if (clazz instanceof Class) {
                    return gson.fromJson(data, (Class) clazz);
                } else {
                    return gson.fromJson(data, (Type) clazz);
                }
            }
        } catch (Exception e) {
            throw new RequestException(e, RequestException.Error.SERVER_DATA_SERIALIZE_ERROR);
        }
    }
}

