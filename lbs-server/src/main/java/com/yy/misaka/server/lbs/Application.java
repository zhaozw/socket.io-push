package com.yy.misaka.server.lbs;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.yy.misaka.server.support.ReplyMessagingTemplate;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.NioEndpoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
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

    /**
     * 设置tomcat连接数
     * @return
     */
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                if (connector.getProtocolHandler() instanceof Http11NioProtocol) {
                    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                    NioEndpoint endpoint = protocol.getEndpoint();
                    endpoint.setMaxConnections(5000000);
                }
                connector.setMaxParameterCount(500);
            }
        });
        return tomcat;
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
