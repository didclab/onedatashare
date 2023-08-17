package org.onedatashare.server.service;

import lombok.SneakyThrows;
import org.onedatashare.server.model.requestdata.BatchJobData;
import org.onedatashare.server.model.requestdata.InfluxData;
import org.onedatashare.server.model.requestdata.MonitorData;
import org.onedatashare.server.model.response.PageImplResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetaDataService {

    @Value("${meta.service.uri}")
    private String metaHostName;

    private Logger logger = LoggerFactory.getLogger(MetaDataService.class);
    private String MONITOR = "/monitor";
    private String USER_EMAIL = "userEmail";
    private String BASE_PATH = "/api/v1/meta";
    private String USER_JOBS = "/user_jobs";
    private String STAT = "/stat";
    private String JOB_ID_QUERY = "jobId";
    private String ALL_STATS = "/all_stats";
    private String INFLUX_RANGE_MEASUREMENTS = "/stats/influx/job/range";
    private String INFLUX_JOB_MEASUREMENTS = "/stats/influx/job";
    private String INFLUX_USER_MEASUREMENTS = "/stats/influx/user";
    private String DATE = "/date";

    @Autowired
    private WebClient.Builder webClientBuilder;

    //CockroachDB data calls below


    @SneakyThrows
    public Mono<List<Long>> getAllJobIds(String userId) {
        logger.info("Querying all user jobs {}", userId);
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + USER_JOBS)
                .queryParam(USER_EMAIL, userId)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Long>>() {
                });
    }

    @SneakyThrows
    public Mono<List<BatchJobData>> getAllStats(String userId) {
        logger.info("the userId we are querying for is {}", userId);
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + ALL_STATS)
                .queryParam(USER_EMAIL, userId)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BatchJobData>>() {
                });
    }

    public Mono<PageImplResponse<BatchJobData>> getAllStats(String userId, Integer page, Integer size, String sort, String direction) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + "/stat/page")
                .queryParam(USER_EMAIL, userId)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort + "," + direction)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageImplResponse<BatchJobData>>() {
                });
    }


    @SneakyThrows
    public Mono<BatchJobData> getJobStat(Long id) {
        logger.info("The jobId we are querying for is {}", id);
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + STAT)
                .queryParam(JOB_ID_QUERY, id)
                .build().toUri();
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(BatchJobData.class);
    }

    public Mono<BatchJobData> getStatByDate(String user, LocalDateTime date) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + STAT + DATE)
                .queryParam(USER_EMAIL, user)
                .queryParam("date", date).build().toUri();
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(BatchJobData.class);
    }

    public Mono<List<BatchJobData>> getStatsByDateRange(String user, LocalDateTime from, LocalDateTime to) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + "/stats/date/range")
                .queryParam(USER_EMAIL, user)
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .build().toUri();
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BatchJobData>>() {
                });
    }

    public Mono<Page<BatchJobData>> getStatsByDateRange(String user, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + "/stats/page/date/range")
                .queryParam(USER_EMAIL, user)
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .queryParam("pageable", pageable)
                .build().toUri();
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Page<BatchJobData>>() {
                });
    }

    public Flux<BatchJobData> getManyJobStats(String user, List<Long> jobIds) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + "/stats/jobIds")
                .queryParam(USER_EMAIL, user);
        for (Long jobId : jobIds) {
            uri.queryParam("jobIds", jobId);
        }

        return this.webClientBuilder.build()
                .get()
                .uri(uri.build().toUri())
                .retrieve()
                .bodyToFlux(BatchJobData.class);
    }

    //All Influx based calls below

    public Mono<List<InfluxData>> measurementsUserJob(String userEmail, Long jobId) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + INFLUX_JOB_MEASUREMENTS)
                .queryParam(USER_EMAIL, userEmail)
                .queryParam("jobId", jobId)
                .build().toUri();
        return influxDataCall(uri);
    }

    public Mono<List<InfluxData>> measurementsByRange(LocalDateTime start, LocalDateTime end, String userEmail) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + INFLUX_RANGE_MEASUREMENTS)
                .queryParam(USER_EMAIL, userEmail)
                .queryParam("start", start)
                .queryParam("end", end)
                .build().toUri();
        return influxDataCall(uri);
    }

    public Mono<List<InfluxData>> userMeasurements(String userEmail) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + INFLUX_USER_MEASUREMENTS)
                .queryParam(USER_EMAIL, userEmail)
                .build().toUri();
        return influxDataCall(uri);
    }

    //checked
    public Mono<MonitorData> monitor(String user, Long jobIds) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + MONITOR)
                .queryParam(USER_EMAIL, user)
                .queryParam("jobId", jobIds)
                .build()
                .toUri();
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(MonitorData.class);
    }


    private Mono<List<InfluxData>> influxDataCall(URI uri) {
        logger.info(uri.toString());
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InfluxData>>() {});
    }

    public Mono<? extends List<InfluxData>> getJobMeasurementsUniversal(String user, Long jobId, LocalDateTime start, String appId) {
        URI uri = UriComponentsBuilder.fromUriString(this.metaHostName)
                .path(BASE_PATH + "/stats/influx/transfer/node")
                .queryParam(USER_EMAIL, user)
                .queryParam("jobId", jobId)
                .queryParam("appId", appId)
                .queryParam("start", start)
                .build()
                .toUri();
        return this.webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<InfluxData>>() {});
    }
}
