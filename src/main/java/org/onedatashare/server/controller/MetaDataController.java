package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.JobStatistic;
import org.onedatashare.server.model.requestdata.InfluxData;
import org.onedatashare.server.service.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetaDataController {

    @Autowired
    MetaDataService metaDataService;

    //CDB calls
    @GetMapping("/job")
    public Mono<List<JobStatistic>> getJobStatistic(Mono<Principal> principalMono, @RequestParam Long jobId) {
        return metaDataService.getJobStat(jobId);
    }

    @GetMapping("/all/job/ids")
    public Mono<List<Long>> getAllJobIds(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllJobIds(user));
    }

    @GetMapping("/all/jobs")
    public Mono<List<JobStatistic>> getAllJobStats(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllStats(user));
    }

    @GetMapping("/all/jobs/range")
    public Mono<List<Integer>> getJobsByDateRange(Mono<Principal> principalMono,
                                                       @RequestParam
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                       @RequestParam
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getStatsByDateRange(user, start, end));
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

}
