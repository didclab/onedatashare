package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    public Mono<JobDetails> getJobsForUser(@RequestBody JobRequest jobDetails){
        return jobService.getJobsForUser(jobDetails);
    }

    /**
     * Handler for queue GET requests
     * @param jobDetails - Request data needed for fetching Jobs
     * @return Mono\<JobDetails\>
     */
    //TODO: Add role annotation for security
    @PostMapping("/admin-jobs")
    public Mono<JobDetails> getJobsForAdmin(@RequestBody JobRequest jobDetails){
        return jobService.getJobForAdmin(jobDetails);
    }

    @PostMapping("/update-user-job")
    public Mono<List<Job>> updateJobsForUser(@RequestHeader HttpHeaders headers, @RequestBody List<UUID> jobIds) {
        return jobService.getUpdatesForUser(jobIds);
    }

    //TODO: Add role annotation for security
    @PostMapping("/update-admin-job")
    public Flux<Job> updateJobsForAdmin(@RequestHeader HttpHeaders headers, @RequestBody List<UUID> jobIds) {
        return jobService.getUpdatesForAdmin(jobIds);
    }

}
