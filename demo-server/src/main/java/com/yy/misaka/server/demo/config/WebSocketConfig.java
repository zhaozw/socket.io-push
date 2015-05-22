//package com.yy.stomp.server.demo.config;
//
//import com.yy.stomp.server.support.PrincipleWebSocketHandler;
//import org.apache.catalina.connector.Connector;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
//import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
//import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
//import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.converter.ByteArrayMessageConverter;
//import org.springframework.messaging.converter.MessageConverter;
//import org.springframework.messaging.converter.StringMessageConverter;
//import org.springframework.messaging.simp.config.ChannelRegistration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
//import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
//
//import java.util.List;
//
//@Configuration
//@EnableScheduling
//@EnableWebSocketMessageBroker
//public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/stomp").setHandshakeHandler(new PrincipleWebSocketHandler()).withSockJS().setHeartbeatTime(3 * 60 * 1000).setSessionCookieNeeded(false);
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
////        registration.taskExecutor().corePoolSize(24).maxPoolSize(24);
//    }
//
//    @Override
//    public void configureClientOutboundChannel(ChannelRegistration registration) {
////        registration.taskExecutor().corePoolSize(64).maxPoolSize(32);
//    }
//
//    @Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/queue/", "/topic/");
//    }
//
//    @Override
//    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
//        messageConverters.add(new ByteArrayMessageConverter());
//        messageConverters.add(new StringMessageConverter());
//        return true;
//    }
//
////    @Bean
////    public EmbeddedServletContainerCustomizer containerCustomizer() {
////        ServerPropertiesAutoConfiguration customizer = new ServerPropertiesAutoConfiguration();
////        return customizer;
////    }
//
//
//    @Bean
//    public WebSocketMessageBrokerStats stats() {
//        return new WebSocketMessageBrokerStats();
//    }
//
//}