package org.onedatashare.server.service;

import org.onedatashare.server.model.core.JobStatistic;
import org.onedatashare.server.model.error.JobIdNotFoundException;
import org.onedatashare.server.model.error.UnknownUserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class MetaDataService {

    @Value("${meta.service.uri}")
    private String metaServiceUrl;
    private static Logger logger = LoggerFactory.getLogger(MetaDataService.class);

    @Autowired
    private WebClient.Builder webClientBuilder;

    public Mono<JobStatistic> getJobStat(Long id){
        logger.info("The jobId we are querying for is {}", id);
        return this.webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder.host(this.metaServiceUrl).path("/stat").queryParam("jobId", id).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new JobIdNotFoundException()))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Internal Server error")))
                .bodyToMono(JobStatistic.class);
    }

    public Mono<List<JobStatistic>> getAllStats(String userId){
        logger.info("the userId we are querying for is {}", userId);
        return this.webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder.host(this.metaServiceUrl).path("/all_stats").queryParam("userId", userId).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new UnknownUserId()))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Internal Server error")))
                .bodyToMono(new ParameterizedTypeReference<List<JobStatistic>>() {});
    }

    public Mono<List<Long>> getAllJobIds(String userId){
        logger.info("Querying all user jobs {}", userId);
        return this.webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder.host(this.metaServiceUrl).path("/user_jobs").queryParam("userId", userId).build())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new UnknownUserId()))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Internal Server error")))
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {});
    }


}
