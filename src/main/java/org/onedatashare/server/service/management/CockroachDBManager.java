package org.onedatashare.server.service.management;

import org.onedatashare.server.model.core.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CockroachDBManager implements Manager{

    @Override
    public Mono<User> createUser(User user){

        return Mono.just(new User());
    }

    @Override
    public Mono<Void> deleteUser(User user){

        return null;
    }

    @Override
    public Mono<Void> setPermissions(){

        return null;
    }

}
