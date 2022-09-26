package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.BatchJobData;
import org.onedatashare.server.model.requestdata.InfluxData;
import org.onedatashare.server.model.requestdata.MonitorData;
import org.onedatashare.server.model.response.PageImplResponse;
import org.onedatashare.server.service.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetaDataController {

    @Autowired
    MetaDataService metaDataService;

    //CDB calls
    @GetMapping("/job")
    public Mono<BatchJobData> getJobStatistic(Mono<Principal> principalMono, @RequestParam Long jobId) {
        return metaDataService.getJobStat(jobId);
    }

    @GetMapping("/all/job/ids")
    public Mono<List<Long>> getAllJobIds(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllJobIds(user));
    }

    @GetMapping("/all/jobs")
    public Mono<List<BatchJobData>> getAllJobStats(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllStats(user));
    }

    @GetMapping("/all/page/jobs")
    public Mono<PageImplResponse<BatchJobData>> getAllJobStats(Mono<Principal> principalMono,
                                                               @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
                                                               @RequestParam(value = "size", defaultValue = "30", required = false) Integer size,
                                                               @RequestParam(value = "sort", defaultValue = "id", required = false) String sort,
                                                               @RequestParam(value = "direction", defaultValue = "DESC", required = false) String direction) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllStats(user, page, size, sort, direction));
    }


    @GetMapping("/job/date")
    public Mono<BatchJobData> getJobByStartDate(Mono<Principal> principalMono, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getStatByDate(user, date));
    }

    @GetMapping("/all/jobs/range")
    public Mono<List<BatchJobData>> getJobsByDateRange(Mono<Principal> principalMono,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getStatsByDateRange(user, start, end));
    }

    @GetMapping("/jobs/id/list")
    public Mono<List<BatchJobData>> getJobsByListOfIds(Mono<Principal> principalMono, @RequestParam(value = "jobId", required = false) List<Long> jobIds) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getManyJobStats(user, jobIds).collectList());
    }

    @GetMapping("/all/page/jobs/range")
    public Mono<Page<BatchJobData>> getJobsByDateRange(Mono<Principal> principalMono,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end, Pageable pageable) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getStatsByDateRange(user, start, end, pageable));
    }


    //Influx Calls
    @GetMapping("/measurements/job")
    public Mono<List<InfluxData>> jobMeasurements(Mono<Principal> principalMono, @RequestParam Long jobId) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.measurementsUserJob(user, jobId));
    }

    @GetMapping("/measurements/user")
    public Mono<List<InfluxData>> allUserMeasurements(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.userMeasurements(user));
    }

    @GetMapping("/measurements/range")
    public Mono<List<InfluxData>> userRangeMeasurements(Mono<Principal> principalMono, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.measurementsByRange(start, end, user));
    }

    @GetMapping("/measurements/monitor")
    public Mono<MonitorData> monitorAJob(Mono<Principal> principalMono, Long jobId) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.monitor(user, jobId));
    }

}
