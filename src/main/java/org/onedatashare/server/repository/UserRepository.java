package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.User;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;


public interface UserRepository extends ReactiveMongoRepository<User, String>{

    @Query(value = "{isAdmin: true}")
    Flux<User> findAllAdministrators();
}
