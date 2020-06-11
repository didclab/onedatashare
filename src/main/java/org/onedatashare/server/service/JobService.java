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


package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.model.jobaction.SearchRequest;
import org.onedatashare.server.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    @Autowired
    private UserService userService;

    @Autowired
    private JobRepository jobRepository;

    private static Pageable generatePageFromRequest(JobRequest request){
        Sort.Direction direction = request.sortOrder.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        request.pageNo = Math.max(request.pageNo, 0);
        Pageable page = PageRequest.of(request.pageNo, request.pageSize, Sort.by(direction, request.sortBy));
        return page;
    }

    private static Pageable generatePageFromRequest(SearchRequest request){
        Sort.Direction direction = request.getSortOrder().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        request.setPageNo(Math.max(request.getPageNo(), 0));
        Pageable page = PageRequest.of(request.getPageNo(), request.getPageSize(), Sort.by(direction, request.getSortBy()));
        return page;
    }

    public Mono<Job> getJobByUUID(UUID uuid) {
        return jobRepository.findById(uuid);
    }

    public Mono<List<Job>> getAllJobsForUser(String cookie) {
        return userService.getJobs(cookie).flatMap(this::getJobByUUID).publishOn(Schedulers.parallel())
                .collectList();
    }

    public Mono<JobDetails> getJobsForUser(JobRequest request) {
        Pageable pageable = this.generatePageFromRequest(request);
        return userService.getLoggedInUserEmail().flatMap(userEmail -> {
            Mono<List<Job>> jobList = jobRepository.findJobsForUser(userEmail, pageable).collectList();
            Mono<Long> jobCount = jobRepository.getJobCountForUser(userEmail);
            return jobList.zipWith(jobCount, JobDetails::new);
        });
    }

    public Mono<JobDetails> getJobForAdmin(JobRequest request){
        Pageable pageable = this.generatePageFromRequest(request);
        return userService.getLoggedInUserEmail().flatMap(userEmail -> {
            Mono<List<Job>> jobList = jobRepository.findAllBy(pageable).collectList();
            Mono<Long> jobCount = jobRepository.getCount();
            return jobList.zipWith(jobCount, JobDetails::new);
        });
    }

    public Mono<JobDetails> getSearchJobs(SearchRequest request) {
        Pageable pageable = this.generatePageFromRequest(request);
        return userService.getLoggedInUserEmail().flatMap(userEmail -> {
            Mono<List<Job>> jobList = jobRepository.findSearchJobs(request.getUsername(), request.progress, request.startJobId, request.endJobId, pageable).collectList();
            Mono<Long> jobCount = jobRepository.getSearchJobsCount(request.getUsername(), request.progress, request.startJobId, request.endJobId);
            return jobList.zipWith(jobCount, JobDetails::new);
        });
    }

    public Mono<List<Job>> getUpdatesForUser(List<UUID> jobIds){
        return userService.getLoggedInUserEmail()
                .flatMap(userEmail ->
                        jobRepository.findAllById(jobIds)
                                .filter(job -> !job.isDeleted() && job.getOwner().equals(userEmail))
                                .collectList()
                );
    }

    public  Flux<Job> getUpdatesForAdmin(List<UUID> jobIds){
        return jobRepository.findAllById(jobIds);
    }

    public Mono<Job> findJobByJobId(String cookie, Integer job_id) {
        return getAllJobsForUser(cookie).map(jobs -> {
            Job job = new Job(null, null);
            for(Job j: jobs) {
                if(j.getJob_id() == job_id) job = j;
            }
            return job;
        });
    }

    public Mono<Job> saveJob(Job job) {
        return jobRepository.save(job);
    }
}
