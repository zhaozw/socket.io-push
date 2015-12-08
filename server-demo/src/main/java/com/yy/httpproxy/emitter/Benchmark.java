package com.yy.httpproxy.emitter;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by xuduo on 12/1/15.
 */
public class Benchmark {

    private static Logger logger = LoggerFactory.getLogger(Benchmark.class);
    private static int numClients = 1000;
    private static String host = "http://183.61.6.33:8080";
    private static String redisHost = "183.61.6.33:6379";
//    private static String host = "http://172.25.133.154:9101";
//    private static String redisHost = "172.25.133.154:6379";
    private static AtomicInteger connected = new AtomicInteger(0);
    private static AtomicLong numRequests = new AtomicLong(0);
    private static AtomicLong seqId = new AtomicLong(0);
    private static long timestamp = 0;
    private static ArrayList<String> clients = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0) {
            host = args[0];
            redisHost = args[1];
            numClients = Integer.parseInt(args[3]);
        }
        final PacketServer server = new PacketServer(redisHost);
        server.addHandler("/testRequest", new PacketHandler() {
            @Override
            void handle(String pushId, String sequenceId, String path, Object body) {
                logger.debug("reply {}", pushId);
                reply(pushId, sequenceId, "/testRequest", body);
            }
        });
        logger.info("benchmarking {} numClients:{}", host, numClients);
        for (int i = 0; i < numClients; i++) {
            try {
                final Socket socket;
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.transports = new String[]{"websocket"};
                socket = IO.socket(host, opts);
                final String id = new BigInteger(130, new SecureRandom()).toString(32);
                clients.add(id);
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        connected.addAndGet(1);
                        try {
                            JSONObject object = new JSONObject();
                            object.put("id", id);
                            logger.debug("connected {}", id);
                            socket.emit("pushId", object);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                socket.on("pushId", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        logger.debug("pushId {}", args);
                        try {
                            request(socket, "/testRequest", "1231231234");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                socket.on("packetProxy", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        logger.debug("packetProxy");
                        long count = numRequests.incrementAndGet();
                        //logger.debug("receive push {}", count);
                        if (count > 0 && count % 10000 == 0) {
                            logger.info("total per second  {} {}", 1000L * count / (System.currentTimeMillis() - timestamp), count);
                        }
                        request(socket, "/testRequest", "1231231234");
                    }
                });
                socket.connect();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        Thread.sleep(2000l);
        timestamp = System.currentTimeMillis();
        while (connected.get() != numClients) {
            Thread.sleep(25l);
        }
        Thread.sleep(2000l);
        logger.info("all connected");



//        for (int i = 0; i < 5; i++) {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
//        while (true) {
//            List<Integer> ids = new ArrayList(clients);
//            for (Integer id : ids) {
//                server.getEmitter().reply("test", id + "", "testdatatttttttttt".getBytes());
//            }
//        }
//                }
//            });
//            t.start();
//        }
//
        Thread.sleep(100000000005435L);

    }

    public static void request(Socket socket, String path, String data) {

        try {

            if (!socket.connected()) {
                return;
            }


            JSONObject object = new JSONObject();
            object.put("data", Base64.encodeBase64String(data.getBytes()));
            object.put("path", path);
            object.put("sequenceId", String.valueOf(seqId.incrementAndGet()));
            socket.emit("packetProxy", object);

            logger.debug("request {}", path);

        } catch (Exception e) {
        }
    }

}
