package org.onedatashare.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {

    @MockBean
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @BeforeAll
    public static void initializeMocks(){
        System.out.println("In before all");
    }

    @Test
    public void getUser_test(){
        String email = "linuscas@buffalo.edu";
        when(userRepository.findById(email)).thenReturn(Mono.just(new User()));
        userService.getUser("linuscas@buffalo.edu")
        .doOnSuccess(user -> assertTrue(user instanceof User));

    }
}
