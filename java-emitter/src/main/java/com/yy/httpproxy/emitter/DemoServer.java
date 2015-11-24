package com.yy.httpproxy.emitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.apache.commons.codec.binary.Base64;
import org.redisson.Redisson;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by xuduo on 11/13/15.
 */
public class DemoServer {


    public static void main(String[] args) throws IOException, InterruptedException {
        PacketServer server = new PacketServer();

        server.addHandler("/addDot", new PacketHandler() {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/addDot", body);
                reply(sequenceId, pushId, headers, body);
            }
        });

        server.addHandler("/endLine", new PacketHandler() {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/endLine", body);
            }
        });

        server.addHandler("/clear", new PacketHandler() {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/clear", null);
            }
        });


        Thread.sleep(100000L);
    }
}
