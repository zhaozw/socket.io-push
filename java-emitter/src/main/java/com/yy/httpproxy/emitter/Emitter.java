package com.yy.httpproxy.emitter;

import com.yy.httpproxy.emitter.protocol.PacketJson;

import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.core.RTopic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import sun.misc.BASE64Encoder;

public class Emitter {

    private BASE64Encoder base64Encoder = new BASE64Encoder();
    private Redisson redisson;
    RTopic<byte[]> rTopic;

    public Emitter(Redisson redisson) {
        this.redisson = redisson;
        rTopic = redisson.getTopic("socket.io#emitter", BytesCodec.INSTANCE);
    }


    public void push(String topic, byte[] data) {

        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("topic", topic);
        if (data != null) {
            jsonRoot.put("data", base64Encoder.encode(data));
        }

        PacketJson packet = new PacketJson();

        packet.setData(jsonRoot);
        packet.setEvent("push");

        rTopic.publishAsync(packet.getBytes());

    }

    public void reply(String sequenceId, String pushId, byte[] data) {

        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("sequenceId", sequenceId);
        jsonRoot.put("code", 1);
        if (data != null) {
            jsonRoot.put("data", base64Encoder.encode(data));
        }

        PacketJson packet = new PacketJson();

        packet.setData(jsonRoot);
        packet.setEvent("packetProxy");
        ArrayList<String> rooms = new ArrayList<>(1);
        rooms.add(pushId);
        packet.setRooms(rooms);

        rTopic.publishAsync(packet.getBytes());

    }


    private static void testJedis(byte[] data) {
        String topic = "test";
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
        Jedis jedis = jedisPool.getResource();

        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("topic", topic);
        jsonRoot.put("data", new BASE64Encoder().encode(data));// base64 {"value":55,"symbol":"yy"}

        PacketJson packet = new PacketJson();

        packet.setData(jsonRoot);
        packet.setEvent("push");

        jedis.publish("socket.io#emitter".getBytes(), packet.getBytes());
    }

    private static void testRedission(byte[] data) {
        String topic = "test";

        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("topic", topic);
        jsonRoot.put("data", new BASE64Encoder().encode(data));// base64 {"value":55,"symbol":"yy"}

        PacketJson packet = new PacketJson();

        packet.setData(jsonRoot);
        packet.setEvent("push");

        RTopic<byte[]> rTopic = Redisson.create().getTopic("socket.io#emitter", BytesCodec.INSTANCE);
        try {
            rTopic.publishAsync(packet.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        RTopic<String> rTopic = Redisson.create().getTopic("test", StringCodec.INSTANCE);
        rTopic.publish("12345");

        testJedis("12345".getBytes());
        testRedission("12345".getBytes());


//        for (int i = 0; i < 1; i++) {
//            new Emitter(Redisson.create()).push("/stock/yy", "{\"value\":55,\"symbol\":\"yy\"}".getBytes());
//            System.out.println(i);
//        }
    }
}