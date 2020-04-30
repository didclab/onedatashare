/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.jobaction.SearchRequest;
import org.onedatashare.server.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PostMapping("/admin-jobs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<JobDetails> getJobsForAdmin(@RequestBody JobRequest jobDetails){
        return jobService.getJobForAdmin(jobDetails);
    }

    /**
     * Handler for queue GET requests
     * @param jobDetails - Request data needed for fetching Jobs as per search
     * @return Mono\<JobDetails\>
     */
    @PostMapping("/search-jobs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<JobDetails> getSearchJobs(@RequestBody SearchRequest jobDetails){
        return jobService.getSearchJobs(jobDetails);
    }

    //TODO: change the function to use query instead of using filter (might make it faster)
    @PostMapping("/update-user-jobs")
    public Mono<List<Job>> updateJobsForUser(@RequestBody List<UUID> jobIds) {
        return jobService.getUpdatesForUser(jobIds);
    }

    @PostMapping("/update-admin-jobs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Flux<Job> updateJobsForAdmin(@RequestBody List<UUID> jobIds) {
        return jobService.getUpdatesForAdmin(jobIds);
    }

}
