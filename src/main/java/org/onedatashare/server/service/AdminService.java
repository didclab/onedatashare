package org.onedatashare.server.service;

import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.onedatashare.server.repository.UserRepository;
import reactor.core.publisher.Mono;

/**
 * Service which backs Admin controller
 * */
@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // this checks the user is an admin or not
    public Mono<Boolean> isAdmin(String email){
        return userRepository.findById(email).map(user ->user.isAdmin());
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }
}
