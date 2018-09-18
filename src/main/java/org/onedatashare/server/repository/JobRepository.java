package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.Job;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface JobRepository extends ReactiveMongoRepository<Job, UUID> {
}
