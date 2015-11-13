package com.yy.httpproxy.emitter;

import java.io.IOException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by xuduo on 11/13/15.
 */
public class PacketHandler {

    private Jedis jedis;

    public PacketHandler() {
        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println(channel + " : " + message);
            }
        }, "packetProxy");
    }

    public static void main(String[] args) throws IOException {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);
        Jedis jedis = jedisPool.getResource();

        jedis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println(channel + " : " + message);
            }
        }, "packetProxy");

        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
