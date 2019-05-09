package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.JobDetails;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
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

    public Mono<JobDetails> getJobsForUserOrAdmin(String cookie, JobRequest request) {
        Sort.Direction direction = request.sortOrder.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        int numberOfItrs = request.pageNo == 0 ? 2 : 3;
        request.pageNo = request.pageNo - 1 < 0 ? 0 : request.pageNo - 1;

        return userService.getLoggedInUser(cookie).flatMap(user -> {
            if(user.isAdmin() && request.status.equals("all")){
                Mono<List<Job>> jobs = Mono.just(new ArrayList<>());
                for (int i = 0; i < numberOfItrs; i++) {
                    int pageNo = request.pageNo + i;
                    jobs = jobs.flatMap(jobs1 -> jobRepository.findAllBy(PageRequest.of(pageNo,
                            request.pageSize, Sort.by(direction, request.sortBy)))
                            .collectList()
                            .map(jobs2 -> {
                                jobs1.addAll(jobs2);
                                return jobs1;
                            }));
                }

                return jobs.flatMap(jobs1 -> jobRepository.count()
                        .map(count ->  {
                            JobDetails result = new JobDetails();
                            result.jobs = jobs1;
                            result.totalCount = count;
                            return result;
                        }));
            }
            else{
                Mono<List<Job>> jobs = Mono.just(new ArrayList<>());
                for (int i = 0; i < numberOfItrs; i++) {
                    int pageNo = request.pageNo + i;
                    jobs = jobs.flatMap(jobs1 -> jobRepository.findJobsForUser(user.email,false, PageRequest.of(pageNo,
                            request.pageSize, Sort.by(direction, request.sortBy)))
                            .collectList()
                            .map(jobs2 -> {
                                jobs1.addAll(jobs2);
                                return jobs1;
                            }));
                }
                return jobs.flatMap(jobs1 -> jobRepository.countJobBy(user.email,false)
                        .map(count ->  {
                            JobDetails result = new JobDetails();
                            result.jobs = jobs1;
                            result.totalCount = count;
                            return result;
                        }));
            }
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
