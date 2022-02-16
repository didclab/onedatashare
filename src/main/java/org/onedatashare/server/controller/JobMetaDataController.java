package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.JobStatistic;
import org.onedatashare.server.service.MetaDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metadata")
public class JobMetaDataController {

    @Autowired
    MetaDataService metaDataService;

    @GetMapping("/all_job_ids")
    public Mono<List<Integer>> getAllJobIds(Mono<Principal> principalMono) {
        return principalMono.map(Principal::getName)
                .flatMap(userId -> metaDataService.fetchAllUserJobIds(userId));
    }

    @GetMapping("/all_jobs")
    public Mono<List<JobStatistic>> getAllJobStats(Mono<Principal> principalMono){
        return principalMono.map(Principal::getName)
                .flatMap(userId -> metaDataService.fetchAllUserJobs(userId));
    }

    @GetMapping("/job")
    public Mono<JobStatistic> getJobStat(Mono<Principal> principalMono, @RequestParam Long jobId){
        return principalMono.map(Principal::getName)
                .flatMap(userId -> metaDataService.fetchOneJob(jobId));
    }
}
