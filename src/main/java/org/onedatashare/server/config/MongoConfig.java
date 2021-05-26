package org.onedatashare.server.config;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
    private final static Logger logger = Logger.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.username}")
    String username;

    @Value("${spring.data.mongodb.password}")
    String password;

    @Value("${spring.data.mongodb.host}")
    String clusterEndpoint;

    @Value("${spring.data.mongodb.database}")
    String database;

    @Bean
    public MongoClient mongoClient(){
        String template = "mongodb://%s:%s@%s/onedatashare?ssl=true&replicaSet=rs0&readpreference=%s";
        String readPreference = "secondaryPreferred";
        String connectionString = String.format(template, username, password, clusterEndpoint,readPreference);
        MongoClientURI clientURI = new MongoClientURI(connectionString);
        MongoClient mongoClient = new MongoClient(clientURI.getURI());
        logger.info(connectionString);
        return mongoClient;
    }
}
