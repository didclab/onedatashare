package org.onedatashare.server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@ComponentScan("org.onedatashare.module")
public class WebConfiguration implements WebFluxConfigurer {
    /*
        Below methods are used for setting up HTTPS on port 8443
     */
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customizer() {
        return new WebServerFactoryCustomizer<NettyReactiveWebServerFactory>() {
            @Override
            public void customize(NettyReactiveWebServerFactory factory) {
                Ssl ssl = new Ssl();
                ssl.setEnabled(true);
                ssl.setKeyStoreType("PKCS12");
                ssl.setKeyStore("classpath:keystore.p12");
                ssl.setKeyPassword("asdasd");
                ssl.setKeyAlias("tomcat");
                ssl.setKeyStorePassword("asdasd");
                factory.setSsl(ssl);
            }
        };
    }
    /*
        Below methods are used for redirecting port 8080 to 8443
     */
    @Autowired
    HttpHandler httpHandler;

    WebServer http;

    @PostConstruct
    public void start() {
        ReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(8080);
        this.http = factory.getWebServer(this.httpHandler);
        this.http.start();
    }

    @PreDestroy
    public void stop() {
        this.http.stop();
    }
}