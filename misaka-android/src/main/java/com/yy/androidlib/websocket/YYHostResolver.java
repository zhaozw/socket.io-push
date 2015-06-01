package com.yy.androidlib.websocket;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by xuduo on 4/23/15.
 */
public class YYHostResolver implements HostResolver {

    private static final String TAG = "YYHostResolver";
    private static final long CACHE_TIMEOUT = 60 * 60 * 1000;
    private String[] dnsList = new String[]{"http://103.227.121.97:19999", "http://120.195.158.34:19999", "http://221.228.79.226:19999"};
    private Context context;

    public YYHostResolver(Context context) {
        this.context = context;
    }

    @Override
    public String resolve(String host) {
        List<String> ips = getIpList(host);
        Log.i(TAG, "ip list " + ips);
        if (ips.size() == 0) {
            return host;
        }
        HttpSpeedTest speedTest = new HttpSpeedTest(ips, 1000l);
        String resolved = speedTest.speedTest(host, true);
        Log.i(TAG, "resolved host " + resolved);
        try {
            URI uri = new URI(resolved);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return host;
        }
    }

    private List<String> getIpList(String host) {
        List<String> ipList = new ArrayList<String>();

        Set<String> ipsFromCache = context.getSharedPreferences("misaka", Context.MODE_PRIVATE).getStringSet("ip_" + host, null);
        long ipsFromCacheTimestamp = context.getSharedPreferences("misaka", Context.MODE_PRIVATE).getLong("ip_time_stamp_" + host, 0);

        if (ipsFromCache != null) {
            Log.v(TAG, "ips from cache " + ipsFromCache);
            if (System.currentTimeMillis() - ipsFromCacheTimestamp < CACHE_TIMEOUT) {
                Log.v(TAG, "not timeout use cache");
                ipList.addAll(ipsFromCache);
                Collections.shuffle(ipList);
                return ipList;
            }
        }

        List<String> resolves = new ArrayList<String>();
        for (String dns : dnsList) {
            resolves.add(dns + "/" + host + ".resolve.html");
        }
        HttpSpeedTest speedTest = new HttpSpeedTest(resolves, 1000l);
        String resolved = speedTest.speedTest(null, false);
        Log.i(TAG, "resolved list " + resolved);


        if (resolved == null) {
            return ipList;
        }
        try {
            Result json = new Gson().fromJson(resolved, Result.class);
            Set<String> ips = new HashSet<String>();
            for (Entry entry : json.addresses) {
                for (String ip : entry.ips) {
                    ips.add("http://" + ip + "/stomp/info");
                }
            }
            context.getSharedPreferences("misaka", Context.MODE_PRIVATE).edit().putStringSet("ip_" + host, ips).commit();
            context.getSharedPreferences("misaka", Context.MODE_PRIVATE).edit().putLong("ip_time_stamp_" + host, System.currentTimeMillis()).commit();
            ipList.addAll(ips);
            Collections.shuffle(ipList);
        } catch (Exception e) {
            Log.e(TAG, "parse json error", e);
        }
        return ipList;
    }

    static class Result {
        List<Entry> addresses;
    }

    static class Entry {
        String type;
        List<String> ips;
    }

}
