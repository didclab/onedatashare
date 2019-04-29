package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends ReactiveMongoRepository<Job, UUID> {

    @Query(value="{$and: [{'owner':?0},{'deleted': ?1}]}")
    Flux<Job> findJobsForUser(String owner, boolean deleted, Pageable pageable);

    @Query(value="{$and: [{'owner':?0},{'deleted': ?1}]}", count = true)
    Mono<Long> countJobBy(String owner, boolean deleted);

}
