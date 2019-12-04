package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.Mail;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MailRepository extends ReactiveMongoRepository<Mail, UUID> {
    Flux<Mail> findAllBy(String id);

    @Query(value = "{status: 'Sent'}")
    Flux<Mail> findAll();

    @Query(value = "{status:'deleted'}")
    Flux<Mail> findAllDeleted();

    @Override
    Mono<Mail> findById(UUID id);

}
