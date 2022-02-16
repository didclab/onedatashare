package org.onedatashare.server.service;

import org.onedatashare.server.model.core.JobStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class MetaDataService {

    @Value("${metadata.service.uri}")
    private String metaDataUrl;
    private Logger logger = LoggerFactory.getLogger(MetaDataService.class);
    private final String API = "/api/v1";
    private final String ALL_STATS= "all_stats";
    private final String ONE_STAT="stat";
    private final String USER_JOBS="user_jobs";
    private final String META = "meta";
    private final String userId = "userId";
    private final String jobId = "jobId";

    @Autowired
    private WebClient.Builder webClientBuilder;

    private WebClient client;
    private String metaDataURLTwoFormat;

    @PostConstruct
    private void initialize() {
        this.metaDataUrl+=API; //the base url from application properties and then "/api/v1/meta"
        logger.info(this.metaDataUrl);
        this.client = this.webClientBuilder.baseUrl(this.metaDataUrl).build();
        this.metaDataURLTwoFormat = "/%s/%s";

    }

    public Mono<List<JobStatistic>> fetchAllUserJobs(String userName){
        logger.info(userName);
        return this.client
                .get()
                .uri(uriBuilder -> uriBuilder
                .path(String.format(metaDataURLTwoFormat, META, ALL_STATS))
                .queryParam(userId, userName)
                .build())
        .retrieve()
        .bodyToFlux(JobStatistic.class)
        .collectList();
    }

    public Mono<JobStatistic> fetchOneJob(Long jobId){
        return this.client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(String.format(this.metaDataURLTwoFormat,META, ONE_STAT))
                        .queryParam(this.jobId, jobId)
                        .build())
                .retrieve()
                .bodyToMono(JobStatistic.class);
    }

    public Mono<List<Integer>> fetchAllUserJobIds(String userId){
        return this.client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(String.format(this.metaDataURLTwoFormat, META, USER_JOBS))
                        .queryParam(this.userId, userId)
                        .build())
                .retrieve()
                .bodyToFlux(Integer.class)
                .collectList();
    }


}
