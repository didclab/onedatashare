package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stork/deleteJob")
public class DeleteJobController {

    @Autowired
    private ResourceServiceImpl resourceService;

    @PostMapping
    public Mono<Job> deleteJob(@RequestHeader HttpHeaders headers, @RequestBody UserAction userAction){
        String cookie = headers.getFirst("cookie");
        return resourceService.deleteJob(cookie, userAction);
    }
}