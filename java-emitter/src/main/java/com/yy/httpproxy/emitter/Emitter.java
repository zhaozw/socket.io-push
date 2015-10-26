package com.yy.httpproxy.emitter;

import com.yy.httpproxy.emitter.protocol.PacketJson;

import org.json.JSONObject;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import sun.misc.BASE64Encoder;

public class Emitter {

    private BASE64Encoder base64Encoder = new BASE64Encoder();
    private Jedis jedis;

    public Emitter(Jedis jedis) {
        this.jedis = jedis;
    }


    public void push(String topic, byte[] data) {

        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("topic", topic);
        jsonRoot.put("data", base64Encoder.encode(data));// base64 {"value":55,"symbol":"yy"}

        PacketJson packet = new PacketJson();

        packet.setData(jsonRoot);
        packet.setEvent("push");

        jedis.publish("socket.io#emitter".getBytes(), packet.getBytes());
    }


    public static void main(String[] args) throws IOException {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
        Jedis jedis = jedisPool.getResource();
        for (int i = 0; i < 100; i++) {
            new Emitter(jedis).push("/stock/yy", "{\"value\":55,\"symbol\":\"yy\"}".getBytes());
            System.out.println(i);
        }
    }
}