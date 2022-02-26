package org.onedatashare.server.service;

import lombok.SneakyThrows;
import org.onedatashare.server.model.core.JobStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
public class MetaDataService {

    @Value("${meta.service.uri}")
    private String metaHostName;
    private Logger logger = LoggerFactory.getLogger(MetaDataService.class);
    private String USER_ID = "userId";
    private String BASE_PATH="/api/v1/meta";
    private String USER_JOBS="/user_jobs";
    private String STAT="/stat";
    private String JOB_ID_QUERY = "jobId";
    private String ALL_STATS="/all_stats";

    @Autowired
    private WebClient.Builder webClientBuilder;

    @SneakyThrows
    public Mono<JobStatistic> getJobStat(Long id){
        logger.info("The jobId we are querying for is {}", id);
        URI uri = new URI(this.metaHostName);
        uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH+ STAT)
                .queryParam(JOB_ID_QUERY, id)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(JobStatistic.class);
    }

    @SneakyThrows
    public Mono<List<JobStatistic>> getAllStats(String userId){
        logger.info("the userId we are querying for is {}", userId);
        URI uri = new URI(this.metaHostName);
        uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH+ ALL_STATS)
                .queryParam(USER_ID, userId)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<JobStatistic>>() {});
    }

    @SneakyThrows
    public Mono<List<Long>> getAllJobIds(String userId){
        logger.info("Querying all user jobs {}", userId);
        URI uri = new URI(this.metaHostName);
        uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH+ USER_JOBS)
                .queryParam(USER_ID, userId)
                .build().toUri();
        logger.info(uri.toString());
         return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {});
    }
}
