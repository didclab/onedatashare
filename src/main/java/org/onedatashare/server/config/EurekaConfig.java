package org.onedatashare.server.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
public class EurekaConfig {

    @Bean
    @Profile("prod")
    @LoadBalanced
    public RestClient.Builder webClientBuilder() {
        return RestClient.builder();

    }

}
