package org.onedatashare.server;
import org.onedatashare.server.model.core.ODSConstants;
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
//@ComponentScan("org.onedatashare.module")
public class WebConfiguration implements WebFluxConfigurer {
    final String CERTIFICATE_KEY_PASSWORD = System.getenv("CERTIFICATE_KEY_PASSWORD");
    /***
        Below methods are used for setting up HTTPS on port 443.
        It loads keystore.p12, which is the certificate for *.onedatashare.org
        uses Key password and KeyStore password set during certificate generation
     */
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> customizer() {
        return new WebServerFactoryCustomizer<NettyReactiveWebServerFactory>() {
            @Override
            public void customize(NettyReactiveWebServerFactory factory) {
                Ssl ssl = new Ssl();
                ssl.setEnabled(true);
                ssl.setKeyStoreType(ODSConstants.CERTIFICATETYPE);
                ssl.setKeyStore(ODSConstants.KEYSTORELOCATION);
                ssl.setKeyPassword(CERTIFICATE_KEY_PASSWORD);
                ssl.setKeyAlias(ODSConstants.CERTIFICATEKEYALIAS);
                ssl.setKeyStorePassword(CERTIFICATE_KEY_PASSWORD);
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
        ReactiveWebServerFactory factory2 = new NettyReactiveWebServerFactory(ODSConstants.REDIRECTHOSTINGPORT);
        this.http = factory2.getWebServer(this.httpHandler);
        this.http.start();
    }

    @PreDestroy
    public void stop() {
        this.http.stop();
    }
}
