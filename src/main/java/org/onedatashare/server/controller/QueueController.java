package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.UUID;

/**
 * Controller for handling GET requests from queue page
 */
@RestController
@RequestMapping("/api/stork/q")
public class QueueController {

    @Autowired
    private JobService jobService;

    /**
     * Handler for queue GET requests
     * @param jobDetails - Request data needed for fetching Jobs
     * @return Mono\<JobDetails\>
     */
    @PostMapping("/user-jobs")
    public Mono<List<Job>> getJobsForUser(@RequestBody JobRequest jobDetails){
        return jobService.getJobsForUserRefactored(jobDetails);
    }

    /**
     * Handler for queue GET requests
     * @param jobDetails - Request data needed for fetching Jobs
     * @return Mono\<JobDetails\>
     */
    //TODO: Add role annotation
    @PostMapping("/admin-jobs")
    public Mono<List<Job>> getJobsForAdmin(@RequestBody JobRequest jobDetails){
        return jobService.getJobForAdminRefactored(jobDetails);
    }

    @PostMapping("/update")
    public Mono<List<Job>> update(@RequestHeader HttpHeaders headers, @RequestBody List<UUID> jobIds) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        return jobService.getUpdates(cookie, jobIds)
                .subscribeOn(Schedulers.elastic());
    }
}
