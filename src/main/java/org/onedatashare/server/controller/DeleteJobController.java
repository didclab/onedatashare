package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.requestdata.JobRequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller that deletes a Job from the queue page
 */
@RestController
@RequestMapping("/api/stork/deleteJob")
public class DeleteJobController {

    @Autowired
    private ResourceServiceImpl resourceService;

    /**
     * Handler for the request for deleting a Job on the queue page.
     *
     * @param headers - Request header
     * @param jobRequestData - Data to perform an operation on the Job
     * @return a map containing all the endpoint credentials linked to the user account as a Mono
     */
    @PostMapping
    public Mono<Job> restartJob(@RequestHeader HttpHeaders headers, @RequestBody JobRequestData jobRequestData){
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        UserAction userAction = UserAction.convertToUserAction(jobRequestData);
        return resourceService.deleteJob(cookie, userAction);
    }
}