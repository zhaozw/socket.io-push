package com.yy.misaka.server.lbs;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.yy.misaka.server.support.ReplyMessagingTemplate;

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

    public static void main(String[] args) {
        final SocketIOServer server = new SocketIOServer(config);
        server.addEventListener("httpProxy", HttpProxyRequest.class, new DataListener<HttpProxyRequest>() {
            @Override
            public void onData(SocketIOClient client, HttpProxyRequest data, AckRequest ackRequest) {
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
                        logger.debug("body ", new String (body));
                        return null;
                    }
                });
                server.getBroadcastOperations().sendEvent("httpProxy", data);
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
        SpringApplication.run(Application.class, args);
    }

}
