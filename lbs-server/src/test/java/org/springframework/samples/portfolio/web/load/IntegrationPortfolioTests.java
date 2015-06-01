///*
// * Copyright 2002-2014 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.springframework.samples.portfolio.web.load;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.eclipse.jetty.client.HttpClient;
//import org.eclipse.jetty.util.thread.QueuedThreadPool;
//import org.eclipse.jetty.websocket.client.WebSocketClient;
//import org.junit.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.core.env.Environment;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.ClientHttpRequest;
//import org.springframework.http.client.ClientHttpResponse;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.converter.MappingJackson2MessageConverter;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.samples.portfolio.web.support.client.StompMessageHandler;
//import org.springframework.samples.portfolio.web.support.client.StompSession;
//import org.springframework.samples.portfolio.web.support.client.WebSocketStompClient;
//import org.springframework.samples.portfolio.web.support.server.TomcatWebSocketTestServer;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RequestCallback;
//import org.springframework.web.client.ResponseExtractor;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
//import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
//import org.springframework.web.socket.sockjs.client.*;
//
//import java.io.IOException;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * End-to-end integration tests that run an embedded Tomcat server and establish
// * an actual WebSocket session using
// * {@link org.springframework.web.socket.client.standard.StandardWebSocketClient}.
// * as well as a simple STOMP/WebSocket client created to support these tests.
// * <p/>
// * The test strategy here is to test from the perspective of a client connecting
// * to a server and therefore it is a much more complete test. However, writing
// * and maintaining these tests is a bit more involved.
// * <p/>
// * An all-encapsulating strategy might be to write the majority of tests using
// * server-side testing (either standalone or with Spring configuration) with
// * end-to-end integration tests serving as a higher-level verification but
// * overall fewer in number.
// *
// * @author Rossen Stoyanchev
// */
//public class IntegrationPortfolioTests {
//
//    private static Log logger = LogFactory.getLog(IntegrationPortfolioTests.class);
//
//    private static int port;
//
//    private static TomcatWebSocketTestServer server;
//
//    private static SockJsClient sockJsClient;
//    private AtomicInteger integer = new AtomicInteger();
//
//    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
//
//
//    @BeforeClass
//    public static void setup() throws Exception {
//
//
//        System.setProperty("spring.profiles.active", "test.tomcat");
//
//    }
//
//    @AfterClass
//    public static void teardown() throws Exception {
//        if (server != null) {
//            try {
//                server.undeployConfig();
//            } catch (Throwable t) {
//                logger.error("Failed to undeploy application", t);
//            }
//
//            try {
//                server.stop();
//            } catch (Throwable t) {
//                logger.error("Failed to stop server", t);
//            }
//        }
//    }
//
//    @Test
//    public void getPositions() throws Exception {
//
//
//        URI uri = new URI("ws://mlbs.yy.com:8080/stomp");
//        for (int i = 0; i < 1; i++) {
//            try {
//                List<Transport> transports = new ArrayList<Transport>();
//                transports.add(new WebSocketTransport(new StandardWebSocketClient()));
//                RestTemplateXhrTransport xhrTransport = new RestTemplateXhrTransport(new RestTemplate());
//                xhrTransport.setRequestHeaders(headers);
//                transports.add(xhrTransport);
//
//                SockJsClient sockJsClient = new SockJsClient(transports);
//                WebSocketStompClient stompClient = new WebSocketStompClient(uri, this.headers, sockJsClient);
//                stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//                stompClient.connect(new StompMessageHandler() {
//                    @Override
//                    public void afterConnected(final StompSession stompSession, StompHeaderAccessor headers) {
//                    }
//
//                    @Override
//                    public void handleMessage(Message<byte[]> message) {
//
//                    }
//
//                    @Override
//                    public void handleError(Message<byte[]> message) {
////				failure.set(new Exception(new String(message.getPayload(), Charset.forName("UTF-8"))));
//                    }
//
//                    @Override
//                    public void handleReceipt(String receiptId) {
//                    }
//
//                    @Override
//                    public void afterDisconnected() {
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                Thread.sleep(1l);
//            } catch (InterruptedException e) {
//                System.out.println();
//            }
//        }
//
//        Thread.sleep(1000000000000l);
//    }
//
//}
