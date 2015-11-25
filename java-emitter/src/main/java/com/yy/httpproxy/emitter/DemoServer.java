package com.yy.httpproxy.emitter;

import java.io.IOException;
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
