package org.onedatashare.server.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EurekaConfig {

    @Bean
    @LoadBalanced
    public WebClient endpointCredentialClient(){
        return WebClient.builder().build();
    }
}
