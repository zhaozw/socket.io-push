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
        SpringApplication.run(Application.class, args);
    }

}
