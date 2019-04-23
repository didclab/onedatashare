package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Flux<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Mono<Job> getJobByUUID(UUID uuid) {
        return jobRepository.findById(uuid);
    }


    public Mono<List<Job>> getAllJobsForUser(String cookie) {
        return userService.getJobs(cookie).flatMap(this::getJobByUUID).publishOn(Schedulers.parallel())
                .collectList();
    }

    public Mono<List<Job>> getJobsForUserOrAdmin(String cookie, String status) {
        return userService.getLoggedInUser(cookie).flatMap(user -> {
            if(user.isAdmin() && status.equals("all")){
                return getAllJobs().collectList();
            }
            else
                return jobRepository.findJobsForUser(userService.cookieToUserLogin(cookie).email,false)
                        .collectList();
        });
    }

    public Mono<Job> findJobByJobId(String cookie, Integer job_id) {
        return getAllJobsForUser(cookie).<Job>map(jobs -> {
            Job job = new Job(null, null);
            for(Job j: jobs) {
                if(j.job_id == job_id) job = j;
            }
            return job;
        });
    }

    public Mono<Job> saveJob(Job job) {
        return jobRepository.save(job);
    }
}
