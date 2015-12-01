package com.yy.httpproxy.emitter;

import java.io.IOException;
import java.util.Map;


/**
 * Created by xuduo on 11/13/15.
 */
public class DemoServer {


    public static void main(String[] args) throws IOException, InterruptedException {

        PacketServer server = new PacketServer();

        Serializer json = new JsonSerializer();
        Serializer byteSerializer = new ByteArraySerializer();
        server.addHandler("/addDot", new PacketHandler<Dot>(Dot.class, json) {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, Dot body) {
                broadcast("/addDot", body);
                reply(sequenceId, pushId, path, headers, body);
            }

        });

        server.addHandler("/endLine", new PacketHandler<byte[]>(byte.class, byteSerializer) {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/endLine", body);
            }
        });

        server.addHandler("/clear", new PacketHandler<byte[]>(byte.class, byteSerializer)  {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/clear", null);
            }
        });


        Thread.sleep(100000L);
    }
}
