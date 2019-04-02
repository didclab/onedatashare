package org.onedatashare.server.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createUser_givenBlankEmailAndPassword_throwsRuntimeExceptionAndDisplaysCorrectMessage() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("No password was provided");
        User user = new User("","");
        userService.createUser(user).subscribe();
    }

    @Test
    public void createUser_givenRandomEmailAndBlankPassword_throwsRuntimeExceptionAndDisplaysCorrectMessage() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("No password was provided");
        User user = new User("ryandils@buffalo.edu","");
        userService.createUser(user).subscribe();
    }

    @Test
    public void createUser_givenBlankEmailAndRandomPassword_throwsNullPointerException() throws Exception {
        thrown.expect(NullPointerException.class);
        User user = new User("","password");
        userService.createUser(user).subscribe();
    }

    @Test
    public void createUser_givenRandomEmailAndPassword_noExceptionThrown() throws Exception {

    }

    @Test
    public void getGlobusClient_givenGlobusClient_returnsGlobsuClient() {

    }

    @Test
    public void removeIfExpired_givenExpiredObject_successfullyRemoves() {

    }

    @Test
    public void verifyEmail_givenValidEmail_successfullyVerifiesEmail() {

    }

    @Test
    public void verifyEmail_givenInvalidEmail_unsuccessfullyVerifiesEmail() {

    }

    @Test
    public void isAdmin_givenAdmin_returnsTrue() {

    }

    @Test
    public void isAdmin_givenSomeoneThatIsNotAdmin_returnsFalse() {

    }

    @Test
    public void getGlobusClient_givenNoCredentials_returnsNull() {

    }
}
