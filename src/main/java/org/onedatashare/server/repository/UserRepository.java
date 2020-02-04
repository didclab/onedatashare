package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserRepository extends ReactiveMongoRepository<User, String>{

    @Query(value = "{'isAdmin': true}", fields = "{'email' : 1, 'firstName' : 1, 'lastName' : 1, 'lastActivity' : 1 }")
    Flux<User> findAllAdministrators(Pageable pageable);

    @Query(value = "{'isAdmin': false}", fields = "{'email' : 1, 'organization' : 1, 'firstName' : 1, 'lastName' : 1, 'validated' : 1, 'registerMoment' : 1, 'lastActivity' : 1}")
    Flux<User> findAllUsers(Pageable pageable);

    @Query(value = "{'isAdmin' : true}", fields = "{'email' : 1}")
    Flux<User> getAllAdminIds();

    @Query(value = "{'isAdmin' : false}", fields = "{'email' : 1}")
    Flux<User> getAllUserEmailIds();

    Flux<User> findAllBy(Pageable pageable);

    @Query(value="{'isAdmin': true}", count = true)
    Mono<Long> countAdministrators();

}
