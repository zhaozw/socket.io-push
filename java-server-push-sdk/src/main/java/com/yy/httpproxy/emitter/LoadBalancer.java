package com.yy.httpproxy.emitter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.redisson.Redisson;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.core.RTopic;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xuduo on 12/2/15.
 */
public class LoadBalancer {

    private Timer timer = new Timer();
    private long timerDelay = 5000L;
    private String serverId = new BigInteger(130, new SecureRandom()).toString(32);
    private Map<String, PacketHandler> handlerMap;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    public static class HandlerInfo {
        public String serverId;
        public String[] paths;
    }

    public LoadBalancer(RedissonReactiveClient redisson, Map<String, PacketHandler> handlerMap) {
        this.handlerMap = handlerMap;
        final RTopicReactive<HandlerInfo> packetServer = redisson.getTopic("packetServer" , new JsonJacksonCodecWithClass(HandlerInfo.class));
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                HandlerInfo info = new HandlerInfo();
                info.serverId = serverId;
                Set<String> paths = LoadBalancer.this.handlerMap.keySet();
                info.paths = paths.toArray(new String[paths.size()]);
                packetServer.publish(info);
            }
        }, 100L, timerDelay);
    }

    public String getServerId() {
        return serverId;
    }
}
