package com.yy.httpproxy.emitter;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.BASE64Encoder;

public class Emitter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private BASE64Encoder base64Encoder = new BASE64Encoder();
    private RedissonReactiveClient redisson;
    //    RTopicReactive<byte[]> rTopic;
    private String prefix = "socket.io#/#";
    private MessagePack msgpack = new MessagePack();

    public Emitter(RedissonReactiveClient redisson) {
        this.redisson = redisson;
//        rTopic = redisson.getTopic("socket.io#emitter", BytesCodec.INSTANCE);
    }


    public void push(String topic, byte[] data) {
        Map dataInfo = new HashMap();
        dataInfo.put("topic", topic);
        if (data != null) {
            dataInfo.put("data", base64Encoder.encode(data));
        }
        byte[] packet = msgPack(topic, "push", dataInfo);
        logger.debug("push packet {} {}", topic, new String(packet));
        getTopic(topic).publish(packet);

    }

    private byte[] msgPack(String topic, String event, Map dataInfo) {
        List list = new ArrayList();
        list.add("emitter");

        Map info = new HashMap();
        info.put("rooms", new String[]{topic});
        info.put("flags", new String[]{});
        info.put("nsp", "/");
        info.put("type", 2);


        List dataList = new ArrayList();
        dataList.add(event);
        dataList.add(dataInfo);
        info.put("data", dataList);
        list.add(info);
        list.add(true);

        //
        // Serialize
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgpack.createPacker(out);
        try {
            packer.write(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = out.toByteArray();
        return bytes;
    }

    private RTopicReactive<byte[]> getTopic(String pushId) {
        return redisson.getTopic(prefix + pushId + "#", BytesCodec.INSTANCE);
    }

    public void reply(String pushId, String sequenceId, byte[] data) {
        Map dataInfo = new HashMap();
        dataInfo.put("sequenceId", sequenceId);
        dataInfo.put("code", 1);
        if (data != null) {
            dataInfo.put("data", base64Encoder.encode(data));
        }
        byte[] packet = msgPack(pushId, "packetProxy", dataInfo);
        logger.debug("reply packet {} {}", pushId, new String(packet));
        getTopic(pushId).publish(packet);

    }

}