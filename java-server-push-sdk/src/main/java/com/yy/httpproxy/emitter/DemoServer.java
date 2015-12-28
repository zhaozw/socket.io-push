package com.yy.httpproxy.emitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DemoServer {

    private static Logger logger = LoggerFactory.getLogger(DemoServer.class);

    public static void main(String[] args) {

        Serializer json = new JsonSerializer();
        Serializer byteSerializer = new ByteArraySerializer();
        PacketServer server = new PacketServer("localhost:6379");
//        PacketServer server = new PacketServer("183.61.6.33:6379");

        server.addHandler(PacketHandler.DISCONNECT, new PacketHandler() {
            @Override
            void handle(String pushId, String sequenceId, String path, Object body) {
                logger.debug("PacketHandler.DISCONNECT {} {}", pushId, path);
            }
        });

        server.addHandler("/addDot", new PacketHandler<Dot>(Dot.class, json) {
            @Override
            void handle(String pushId, String sequenceId, String path, Dot body) {
                broadcast("/addDot", body);
                reply(pushId, sequenceId, path, body);
            }
        });

        server.addHandler("/endLine", new PacketHandler<byte[]>(byte.class, byteSerializer) {
            @Override
            void handle(String pushId, String sequenceId, String path, byte[] body) {
                broadcast("/endLine", body);
            }
        });

        server.addHandler("/clear", new PacketHandler<byte[]>(byte.class, byteSerializer) {
            @Override
            void handle(String pushId, String sequenceId, String path, byte[] body) {
                broadcast("/clear", null);
            }
        });

            Thread.sleep(100000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}