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
@RequestMapping("/api/metadata")
public class MetaDataController {

    @Autowired
    MetaDataService metaDataService;

    @GetMapping("/job")
    public Mono<JobStatistic> getJobStatistic(Mono<Principal> principalMono, @RequestParam Long jobId){
        return metaDataService.getJobStat(jobId);
    }

    @GetMapping("/all/jobids")
    public Mono<List<Long>> getAllJobIds(Mono<Principal> principalMono){
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllJobIds(user));
    }

    @GetMapping("/all/jobs")
    public Mono<List<JobStatistic>> getAllJobStats(Mono<Principal> principalMono){
        return principalMono.map(Principal::getName)
                .flatMap(user -> metaDataService.getAllStats(user));
    }
}
