package org.onedatashare.server.repository;

import org.onedatashare.server.model.core.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserRepository extends ReactiveMongoRepository<User, String>{
}
