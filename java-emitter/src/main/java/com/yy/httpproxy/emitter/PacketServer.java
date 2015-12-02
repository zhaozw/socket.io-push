package com.yy.httpproxy.emitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.commons.codec.binary.Base64;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by xuduo on 11/13/15.
 */
public class PacketServer {

    private Emitter emitter;
    private Map<String, PacketHandler> handlerMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Redisson redisson;
    private LoadBalancer loadBalancer;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type",
            defaultImpl = PackProxy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackProxy {
        public String path;
        public String data;
        public String sequenceId;
        public String pushId;

        @Override
        public String toString() {
            return "PackProxy{" +
                    "path='" + path + '\'' +
                    ", data='" + data + '\'' +
                    ", sequenceId='" + sequenceId + '\'' +
                    ", pushId='" + pushId + '\'' +
                    '}';
        }
    }

    public PacketServer(String host) {
        Config config = new Config();
        config.useSingleServer().setAddress(host);
        redisson = Redisson.create(config);
        emitter = new Emitter(redisson);
        loadBalancer = new LoadBalancer(redisson, handlerMap);
        RTopic<PackProxy> topic = redisson.getTopic("packetProxy#" + loadBalancer.getServerId(), new JsonJacksonCodecWithClass(PackProxy.class));
        topic.addListener(new MessageListener<PackProxy>() {

            public void onMessage(String channel, PackProxy message) {
                logger.debug("onMessage {}", channel, message);
                PacketHandler handler = handlerMap.get(message.path);
                if (handler != null) {
                    handler.handleBinary(message.pushId, message.sequenceId, message.path, Base64.decodeBase64(message.data));
                }
            }
        });



    }

    public void addHandler(String path, PacketHandler handler) {
        handler.setEmitter(emitter);
        handlerMap.put(path, handler);
    }

    public Emitter getEmitter() {
        return emitter;
    }
}
