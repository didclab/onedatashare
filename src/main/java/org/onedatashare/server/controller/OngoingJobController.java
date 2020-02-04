package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.requestdata.JobRequestData;
import org.onedatashare.server.model.requestdata.OngoingJobRequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Controller that request current ongoing jobs from existing jobs.
 * Invoked when user clicks have transfers on the main page
 */
@RestController
@RequestMapping("/api/stork/ongoingjobs")
public class OngoingJobController {

    @Autowired
    private ResourceServiceImpl resourceService;

    /**
     * Handler that return user's all ongoing job's details.
     *
     * @param headers - Incoming request headers
     * @return JobDetails - Ongoing Jobs by the current user
     */
    @PostMapping
    public Mono<JobDetails> ongoingJobForUser(@RequestHeader HttpHeaders headers) {
        String cookie = headers.getFirst(ODSConstants.COOKIE);
        return resourceService.ongoingJobForUser(cookie);
    }
}
