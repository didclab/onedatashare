package org.onedatashare.server.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("UserService.createUser")
    @Disabled
    // Check the test cases
    public void createUserTest() {
        // No email and password provided

        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("", ""));
                }, "allows creation of user with no email and password provided"
        );
        // Only password provided
        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("", "abcdefgh")).subscribe();
                }, "allows creation of user with no email provided"
        );
        // Only email provided
        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("a@e.com", "")).subscribe();
                }, "allows creation of user with no password provided"
        );
        // Short password provided
        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("", "abcd")).subscribe();
                }, "allows creation of user with short password"
        );

        // Invalid email format provided
        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("invalidEmail", "abcdefgh")).subscribe();
                }, "allows creation of user with invalid email format"
        );

        assertThrows(NullPointerException.class, () -> {
                    userService.createUser(new User("abcdefgh@gmail.com", "helloWorld")).subscribe();
                }, "allows creation of user with email and password provided"
        );
    }


}
