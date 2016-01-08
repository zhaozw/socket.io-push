package com.yy.httpproxy.stats;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuduo on 1/8/16.
 */
public class Stats {

    private Map<String,Performance> performances = new HashMap<>();

    public void reportError(String path) {
        Performance performance = getPerformance(path);
        performance.addError();
    }

    private Performance getPerformance(String path) {
        Performance performance = performances.get(path);
        if (performance == null) {
            performance = new Performance();
            performance.path = path;
            performances.put(path, performance);
        }
        return performance;
    }

    public void reportSuccess(String path, long timestamp) {
        Performance performance = getPerformance(path);
        performance.addSuccess(System.currentTimeMillis() - timestamp);
    }

    public JSONArray getRequestJsonArray() throws JSONException {
        JSONArray array = new JSONArray();
        for (Performance performance : performances.values()) {
            JSONObject object = new JSONObject();
            object.put("path", performance.path);
            object.put("successCount", performance.successCount);
            object.put("totalCount", performance.errorCount + performance.successCount);
            object.put("totalLatency", performance.totalLatency);
            array.put(object);
        }
        performances.clear();
        return array;
    }
}
