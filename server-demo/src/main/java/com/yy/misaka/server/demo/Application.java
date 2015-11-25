package com.yy.misaka.server.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.ning.http.client.AsyncHttpClient;
import com.yy.misaka.support.BroadcastService;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.NioEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableAsync
public class Application extends WebMvcConfigurerAdapter {

    @Autowired
    Environment env;


    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass(env.getProperty("jdbc.driverClassName")); //loads the jdbc driver
        cpds.setJdbcUrl(env.getProperty("jdbc.url"));
        cpds.setUser(env.getProperty("jdbc.username"));
        cpds.setPassword(env.getProperty("jdbc.password"));

// the settings below are optional -- c3p0 can work with defaults
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);

        return cpds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPersistenceUnitName("my_pu");
        lef.setPackagesToScan("com.yy.misaka.server.demo.domain");
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter);
        lef.setJpaProperties(getJpaProperties());
        return lef;
    }

    @Bean
    public BroadcastService broadcastService() {
        BroadcastService broadcastService = new BroadcastService();
        broadcastService.setHost("http://dev.yypm.com:8080");
        return broadcastService;
    }

    @Bean
    public AsyncHttpClient asyncHttpClient() {
        return new AsyncHttpClient();
    }


    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();

        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");

        return jpaVendorAdapter;
    }

    private Properties getJpaProperties() {
        return new Properties() {
            {
                setProperty("hibernate.hbm2ddl.auto", "update");
                setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                setProperty("hibernate.show_sql", "false");
                setProperty("hibernate.format_sql", "true");
            }
        };
    }

    @Bean
    public ObjectMapper messagingTemplate() {
        return new ObjectMapper();
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                if (connector.getProtocolHandler() instanceof Http11NioProtocol) {
                    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                    NioEndpoint endpoint = protocol.getEndpoint();
                    endpoint.setMaxConnections(500000);
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
