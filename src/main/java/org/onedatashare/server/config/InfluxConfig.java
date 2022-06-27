package org.onedatashare.server.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {

    @Value("${influxdb.url}")
    private String INFLUXDB_URL;

    @Value("${influxdb.org}")
    private String INFLUXDB_ORG;

    @Value("${influxdb.token}")
    private String INFLUXDB_TOKEN;

    @Bean
    public InfluxDBClient influxClient() {
        InfluxDBClientOptions influxDBClientOptions = InfluxDBClientOptions.builder()
                .url(INFLUXDB_URL)
                .org(INFLUXDB_ORG)
                .authenticateToken(INFLUXDB_TOKEN.toCharArray())
                .build();
        return InfluxDBClientFactory.create(influxDBClientOptions);
    }
}
