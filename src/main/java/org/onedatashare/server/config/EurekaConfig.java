package org.onedatashare.server.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EurekaConfig {

    @Bean
    @Profile("dev")
    public WebClient endpointCredentialClient(){
        return WebClient.builder().build();
    }


    @Bean
    @Profile("prod")
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(-1));

    }

}
