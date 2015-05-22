//package com.yy.stomp.server.demo.config;
//
//import java.util.List;
//
//import org.apache.http.HttpHost;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.conn.routing.HttpRoute;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
//import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
//import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
//import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
//import org.apache.http.impl.nio.reactor.IOReactorConfig;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.AsyncClientHttpRequestFactory;
//import org.springframework.http.client.ClientHttpRequestFactory;
//import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.web.client.AsyncRestTemplate;
//import org.springframework.web.client.RestTemplate;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Configuration
//public class HttpConfig {
//
//    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
//
//    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 5;
//
//    private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (60 * 1000);
//
//
//    // ################################################### SYNC
//    @Bean
//    public ClientHttpRequestFactory httpRequestFactory() {
//        return new HttpComponentsClientHttpRequestFactory(httpClient());
//    }
//
//    @Bean
//    public RestTemplate restTemplate() {
//        RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
//        return restTemplate;
//    }
//
//    @Bean
//    public CloseableHttpClient httpClient() {
//        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
//        connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
//        connectionManager
//                .setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
//        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                "facebook.com")), 20);
//        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                "twitter.com")), 20);
//        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                "linkedin.com")), 20);
//        connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                "viadeo.com")), 20);
//        RequestConfig config = RequestConfig.custom()
//                .setConnectTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS).build();
//
//        CloseableHttpClient defaultHttpClient = HttpClientBuilder.create()
//                .setConnectionManager(connectionManager)
//                .setDefaultRequestConfig(config).build();
//        return defaultHttpClient;
//    }
//
//    // ################################################### ASYNC
//    @Bean
//    public AsyncClientHttpRequestFactory asyncHttpRequestFactory() {
//        return new HttpComponentsAsyncClientHttpRequestFactory(
//                asyncHttpClient());
//    }
//
//    @Bean
//    public RestTemplate asyncRestTemplate() {
////        AsyncRestTemplate restTemplate = new AsyncRestTemplate(
////                asyncHttpRequestFactory(), restTemplate());
////        AsyncRestTemplate restTemplate = new AsyncRestTemplate();
//        RestTemplate restTemplate = new RestTemplate();
//        return restTemplate;
//    }
//
//    @Bean
//    public CloseableHttpAsyncClient asyncHttpClient() {
//        try {
//            PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(
//                    new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
//            connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
//            connectionManager
//                    .setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
//            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                    "facebook.com")), 20);
//            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                    "twitter.com")), 20);
//            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                    "linkedin.com")), 20);
//            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(
//                    "viadeo.com")), 20);
//            RequestConfig config = RequestConfig.custom()
//                    .setConnectTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS)
//                    .build();
//
//            CloseableHttpAsyncClient httpclient = HttpAsyncClientBuilder
//                    .create().setConnectionManager(connectionManager)
//                    .setDefaultRequestConfig(config).build();
//            return httpclient;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//}
