package org.onedatashare.server.service;

import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import org.onedatashare.server.repository.UserRepository;

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

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }
}
