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
    private static int numClients = 1000;
    private static int numOfPushes = 1000;
    private static String host = "http://61.147.186.58:80";
    private static String redisHost = "61.147.186.58:6379";
    private static AtomicInteger connected = new AtomicInteger(0);
    private static AtomicInteger numRequests = new AtomicInteger(0);
    private static AtomicInteger seqId = new AtomicInteger(0);
    private static Socket lastSocket;
    private static long timestamp = 0;

    public static void main(String[] args) throws InterruptedException {
        logger.info("benchmarking {} numClients:{}", host, numClients);
        if (args.length > 0) {
            host = args[0];
            redisHost = args[1];
            numClients = Integer.parseInt(args[3]);

        }
        for (int i = 0; i < numClients; i++) {
            try {
                final Socket socket;
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.transports = new String[]{"websocket"};
                socket = IO.socket(host, opts);
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        connected.addAndGet(1);
                        JSONObject data = new JSONObject();
                        try {
                            logger.debug("connected");
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
                        int count = numRequests.incrementAndGet();
                        logger.debug("receive push {}", count);
                        if (count == numClients * numOfPushes) {
                            logger.info("total {} per second  {} time {}ms ", numClients * numOfPushes, 1000L * count / (System.currentTimeMillis() - timestamp), (System.currentTimeMillis() - timestamp) / 1000f);
                        }
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
        Thread.sleep(2000l);
        timestamp = System.currentTimeMillis();
        Serializer json = new ByteArraySerializer();
        PacketServer server = new PacketServer(redisHost);
//        server.addHandler("/addDot", new PacketHandler() {
//            @Override
//            void handle(String pushId, String sequenceId, String path, Object[] body) {
//                broadcast("/addDot", body);
//                //reply(sequenceId, pushId, path, headers, body);
//            }
//        });

        for (int i = 0; i < numOfPushes; i++) {
//            request(lastSocket, "/addDot", "testdatatttttttttt");
            server.getEmitter().push("/addDot", "testdatatttttttttt".getBytes());
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
