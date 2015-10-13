package com.yy.misaka.server.lbs.socketio;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.yy.misaka.server.support.ReplyMessagingTemplate;

import org.redisson.Config;
import org.redisson.Redisson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class Server {


    private int timeout;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public SocketIOServer socketIOServer() {
        logger.info("start initialize server");

        timeout = 3000;
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectTimeout(timeout).setReadTimeout(timeout).setRequestTimeout(timeout).build());

        Config configRedisson = new Config();
        configRedisson.useSingleServer().setAddress("127.0.0.1:6379");
        //  config.useSingleServer().setAddress("dev.yypm.com:7000");

//        Redisson redisson = Redisson.create(configRedisson);

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//        config.setStoreFactory(new RedissonStoreFactory(redisson));
        config.setPort(8080);
        final SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("httpProxy", ProxyRequest.class, new DataListener<ProxyRequest>() {
            @Override
            public void onData(SocketIOClient client, final ProxyRequest data, AckRequest ackRequest) {
                // broadcast messages to all clients

                logger.debug("recieved httpProxy event {}", data);
                AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(data.getUrl());
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                // builder.addQueryParam("data","12345");
                builder.setBody("data=11111");
                asyncHttpClient.executeRequest(builder.build(), new AsyncCompletionHandler<Object>() {
                    @Override
                    public Object onCompleted(Response response) throws Exception {
                        byte[] body =
                                response.getResponseBodyAsBytes();
                        logger.debug("body ", new String(body));
                        ProxyResponse proxyResponse = new ProxyResponse();
                        proxyResponse.setResponse("{data='test555'}");
                        proxyResponse.setSequenceId(data.getSequenceId());
                        server.getBroadcastOperations().sendEvent("httpProxy", proxyResponse);
                        return null;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        logger.error("onThrowable", t);
                        ProxyResponse proxyResponse = new ProxyResponse();
                        proxyResponse.setResponse(new String(""));
                        proxyResponse.setSequenceId(data.getSequenceId());
                        proxyResponse.setResponseCode(-1);
                        proxyResponse.setReponseMessage(t.getMessage());
                        server.getBroadcastOperations().sendEvent("httpProxy", proxyResponse);
                    }

                });
            }
        });

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                logger.info(" client connected {}", client.getSessionId());

            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                logger.info(" client disconnected {}", client.getSessionId());
            }
        });

        server.start();

        return server;
    }
}
