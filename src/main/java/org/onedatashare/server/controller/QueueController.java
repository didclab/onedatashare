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
     * @param headers - Incoming request headers
     * @param jobDetails - Request data needed for fetching Jobs
     * @return Mono\<JobDetails\>
     */
    @PostMapping
    public Mono<JobDetails> queue(@RequestHeader HttpHeaders headers, @RequestBody JobRequest jobDetails) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        if(jobDetails.status.equals("all")) {
            return jobService.getJobsForAdmin(cookie, jobDetails).subscribeOn(Schedulers.elastic());
        }
        else
            return jobService.getJobsForUser(cookie, jobDetails)
                    .subscribeOn(Schedulers.elastic());
    }

    /**
     *  Code for accessing security context
     *             return ReactiveSecurityContextHolder.getContext()
     *                     .map(SecurityContext::getAuthentication)
     *                     .map(x -> {
     *                         System.out.println(x.toString());
     *                         return x.toString();
     *                     }).map(x -> {
     *                         return new JobDetails();
     *             });
     */

// //To be enabled after changing frontend
//    @PostMapping("/userJobs")
//    public Mono<List<Job>> userQueue(@RequestHeader HttpHeaders headers, @RequestBody JobRequest jobDetails){
//        String cookie = headers.getFirst(ODSConstants.COOKIE);
//        return jobService.getJobsForUserRefactored(cookie, jobDetails);
//    }
//
//    @PostMapping("/adminJobs")
//    public Mono<List<Job>> adminQueue(@RequestHeader HttpHeaders headers, @RequestBody JobRequest jobDetails){
//        String cookie = headers.getFirst(ODSConstants.COOKIE);
//        return jobService.getJobForAdminRefactored(cookie, jobDetails);
//    }

    @PostMapping("/update")
    public Mono<List<Job>> update(@RequestHeader HttpHeaders headers, @RequestBody List<UUID> jobIds) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        return jobService.getUpdates(cookie, jobIds)
                .subscribeOn(Schedulers.elastic());
    }
}
