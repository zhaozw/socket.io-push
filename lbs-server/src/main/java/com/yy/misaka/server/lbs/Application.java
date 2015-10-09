package com.yy.misaka.server.lbs;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.yy.misaka.server.lbs.socketio.ProxyRequest;
import com.yy.misaka.server.lbs.socketio.ProxyResponse;
import com.yy.misaka.server.support.ReplyMessagingTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableAsync
public class Application extends WebMvcConfigurerAdapter {

    private int timeout;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public ReplyMessagingTemplate messagingTemplate() {
        return new ReplyMessagingTemplate();
    }

    @Bean
    public AsyncHttpClient asyncHttpClient() {
        timeout = 3000;
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectTimeout(timeout).setReadTimeout(timeout).setRequestTimeout(timeout).build());
        return asyncHttpClient;
    }


//    @Bean
//    public SocketIOServer discardServer() throws Exception {
//        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
//        config.setHostname("localhost");
//        config.setPort(9092);
//
//        final SocketIOServer server = new SocketIOServer(config);
//        server.addEventListener("chatevent", ProxyRequest.class, new DataListener<ProxyRequest>() {
//            @Override
//            public void onData(SocketIOClient client, ProxyRequest data, AckRequest ackRequest) {
//                // broadcast messages to all clients
//                server.getBroadcastOperations().sendEvent("chatevent", data);
//            }
//        });
//
//        server.addEventListener("msg", byte[].class, new DataListener<byte[]>() {
//            @Override
//            public void onData(SocketIOClient client, byte[] data, AckRequest ackRequest) {
//                client.sendEvent("msg", new String(data));
//            }
//        });
//
//        server.addEventListener("httpProxy", ProxyRequest.class, new DataListener<ProxyRequest>() {
//            @Override
//            public void onData(SocketIOClient client, ProxyRequest data, AckRequest ackRequest) {
//                // broadcast messages to all clients
//                server.getBroadcastOperations().sendEvent("chatevent", data);
//            }
//        });
//
//        server.start();
//        return server;
//    }

    @Bean
    public SocketIOServer socketIOServer(final AsyncHttpClient asyncHttpClient) {

        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
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
                        proxyResponse.setResponse(new String(body));
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


    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

}
