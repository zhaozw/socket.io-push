package com.yy.httpproxy.emitter;

import com.yy.httpproxy.emitter.protocol.PacketJson;

import org.json.JSONObject;
import org.redisson.Redisson;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.core.RTopic;

import java.io.IOException;
import java.util.ArrayList;

import sun.misc.BASE64Encoder;

public class Emitter {

    private BASE64Encoder base64Encoder = new BASE64Encoder();
    RTopicReactive<byte[]> rTopic;

    public Emitter(RedissonReactiveClient redisson) {
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
        ArrayList<String> rooms = new ArrayList<>(1);
        rooms.add(topic);
        packet.setRooms(rooms);

        rTopic.publish(packet.getBytes());

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

        rTopic.publish(packet.getBytes());

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

        testRedission("12345".getBytes());


//        for (int i = 0; i < 1; i++) {
//            new Emitter(Redisson.create()).push("/stock/yy", "{\"value\":55,\"symbol\":\"yy\"}".getBytes());
//            System.out.println(i);
//        }
    }
}