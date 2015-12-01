package com.yy.httpproxy.emitter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by xuduo on 12/1/15.
 */
public class Benchmark {

    private static Logger logger = LoggerFactory.getLogger(Benchmark.class);
    private static int numClients = 200;
    private static String host = "http://localhost:9101";
    private static AtomicInteger connected = new AtomicInteger(0);
    private static AtomicInteger numRequests = new AtomicInteger(0);
    private static AtomicInteger seqId = new AtomicInteger(0);
    private static Socket lastSocket;

    public static void main(String[] args) throws InterruptedException {
        logger.info("benchmarking {} numClients:{}", host, numClients);
        for (int i = 0; i < numClients; i++) {
            try {
                final Socket socket;
                IO.Options opts = new IO.Options();
                opts.transports = new String[]{"websocket"};
                socket = IO.socket(host, opts);
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        connected.addAndGet(1);
                        JSONObject data = new JSONObject();
                        try {
                            data.put("topic", "/addDot");
                            socket.emit("subscribeTopic", data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                socket.on("push", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        logger.info("receive push {} {}", numRequests.incrementAndGet(), args[0]);
                    }
                });
                socket.connect();
                lastSocket = socket;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        while (connected.get() != numClients) {
            Thread.sleep(100l);
        }

        Serializer json = new ByteArraySerializer();
        PacketServer server = new PacketServer();
        server.addHandler("/addDot", new PacketHandler<byte[]>(byte[].class, json) {
            @Override
            void handle(String uid, String pushId, String sequenceId, String path, Map<String, String> headers, byte[] body) {
                broadcast("/addDot", body);
                reply(sequenceId, pushId, path, headers, body);
            }
        });

        for (int i = 0; i < 1000; i++) {
            request(lastSocket, "/addDot", "testdatatttttttttt");
        }

        Thread.sleep(100000L);

    }

    public static void request(Socket socket, String path, String data) {

        try {


            if (!socket.connected()) {
                return;
            }


            JSONObject headers = new JSONObject();

            JSONObject object = new JSONObject();
            object.put("headers", headers);
            object.put("data", data);
            object.put("path", path);
            object.put("sequenceId", String.valueOf(seqId.incrementAndGet()));

            socket.emit("packetProxy", object);


        } catch (Exception e) {
        }
    }

}
