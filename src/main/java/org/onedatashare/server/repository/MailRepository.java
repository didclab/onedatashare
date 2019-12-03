package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.Mail;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface MailRepository extends ReactiveMongoRepository<Mail, UUID> {
    Flux<Mail> findAllBy(String id);
}
