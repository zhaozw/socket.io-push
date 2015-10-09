package com.yy.misaka.server.lbs;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.yy.misaka.server.support.ReplyMessagingTemplate;

import org.redisson.Config;
import org.redisson.Redisson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

//    @Bean
//    public AsyncHttpClient asyncHttpClient() {
//        timeout = 3000;
//        AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setConnectTimeout(timeout).setReadTimeout(timeout).setRequestTimeout(timeout).build());
//        return asyncHttpClient;
//    }
//
//
//    @Bean
//    public StoreFactory reddison(){
//        Config config = new Config();
//        config.useSingleServer().setAddress("127.0.0.1:6379");
//      //  config.useSingleServer().setAddress("dev.yypm.com:7000");
//
//        Redisson redisson = Redisson.create(config);
//        return new RedissonStoreFactory(redisson);
//    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
