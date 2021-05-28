package org.onedatashare.server.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientOptions mongoOptions(){
        return MongoClientOptions.builder()
                .socketTimeout(120)
                .build();
    }
}
