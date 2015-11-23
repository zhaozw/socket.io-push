package com.yy.httpproxy.emitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.parser.JSONParser;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

/**
 * Created by xuduo on 11/13/15.
 */
public class PacketServer {

    private Jedis jedis;
    private Emitter emitter;
    private Map<String, PacketHandler> handlerMap = new HashMap<>();
    private JSONParser parser = new JSONParser();
    private Redisson redisson = Redisson.create();

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type",
            defaultImpl = PackProxy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackProxy {
        public String path;
        public String body;
        public String sequenceId;
    }

    public PacketServer() {
//        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
//        jedis = jedisPool.getResource();
        emitter = new Emitter(redisson);
        RTopic<PackProxy> topic = redisson.getTopic("packetProxy", new JsonJacksonCodecWithClass(PackProxy.class));
        topic.addListener(new MessageListener<PackProxy>() {

            public void onMessage(String channel, PackProxy message) {
                PacketHandler handler = handlerMap.get(message.path);
                if (handler != null) {
                    handler.handle(message.sequenceId, message.path, null, Base64.decodeBase64(message.body));
                }
            }
        });
    }

    public void addHandler(String path, PacketHandler handler) {
        handler.setEmitter(emitter);
        handlerMap.put(path, handler);
    }

//    public void start(){
//        jedis.subscribe(new JedisPubSub() {
//            @Override
//            public void onMessage(String channel, String message) {
//                System.out.println(channel + " : " + message);
//                try {
//                    JSONObject object = (JSONObject) parser.parse(message);
//                    String path = (String) object.get("path");
//                    String sequenceId = (String) object.get("sequenceId");
//                    String body = (String) object.get("body");
//                    PacketHandler handler = handlerMap.get(path);
//                    if (handler != null) {
//                        handler.handle(sequenceId, path, null, Base64.decodeBase64(body));
//                    }
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, "packetProxy");
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        PacketServer server = new PacketServer();

        server.addHandler("/addDot", new PacketHandler() {
            @Override
            void handle(String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/addDot", body);
            }
        });

        server.addHandler("/endLine", new PacketHandler() {
            @Override
            void handle(String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/endLine", body);
            }
        });

        server.addHandler("/clear", new PacketHandler() {
            @Override
            void handle(String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/clear", null);
            }
        });


        Thread.sleep(100000L);
    }
}
