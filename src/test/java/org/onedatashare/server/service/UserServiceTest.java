package org.onedatashare.server.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("User Service Tests")
public class UserServiceTest {

    @MockBean
    UserRepository userRepository;

    @Autowired
    UserService userService;

    User user;

    @BeforeEach
    public void initTest(){
        user = new User();
        user.setEmail("ods_test_user@test.com");
        user.setFirstName("ODS_Test");
        user.setLastName("User");
        user.setAdmin(false);
    }

    @Nested
    @DisplayName("getUser()")
    class GetUserTest{
        String validInputEmail = "ods_test_user@test.com";
        String invalidInputEmail = "nonexistent_ods_test_user@test.com";

        @BeforeEach
        public void initGetUserTest(){
            // Return values decided by referring official documentation of ReactiveCrudRepository
            // https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/reactive/ReactiveCrudRepository.html
            when(userRepository.findById(user.getEmail())).thenReturn(Mono.just(user));
            when(userRepository.findById(invalidInputEmail)).thenReturn(Mono.empty());
            when(userRepository.findById("")).thenReturn(Mono.empty());
            doThrow(IllegalArgumentException.class).when(userRepository).findById((String) null);
        }

        @Test
        @DisplayName("Valid user email")
        public void getUser_test_validEmail(){
            userService.getUser(validInputEmail)
                    .doOnSuccess(user -> {
                        assertTrue(user instanceof User, "Expected to receive a User object");
                        assertEquals(validInputEmail, user.getEmail(),"Did not retrieve the expected user");
                    });
        }

        @Test
        @DisplayName("Invalid user email")
        public void getUser_test_invalidEmail(){
            userService.getUser(invalidInputEmail)
                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
                    .doOnError(error -> {
                        assertTrue(error instanceof Exception, "Did not receive expected error");
                        assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail,"Expected error not encountered");
                    });
        }

        @Test
        @DisplayName("Blank user email")
        public void getUser_test_blankEmail(){
            userService.getUser("")
                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
                    .doOnError(error -> {
                        assertTrue(error instanceof Exception, "Did not receive expected error");
                        assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail,"Expected error not encountered");
                    });
        }


        // Test case not handled in original code. Need to fix!!!!!!
//        @Test
//        @DisplayName("Null user email input")
//        public void getUser_test_nullEmail(){
//            userService.getUser(null)
//                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
//                    .doOnError(error -> {
//                        assertTrue(error instanceof IllegalArgumentException, "Did not receive expected error")
//                    });
//        }
    }    // GetUserTest

    @Nested
    @DisplayName("login()")
    class LoginTest{

        @Test
        @DisplayName("Valid email, Valid Password")
        public void login_test_validInput(){

        }

        @Test
        @DisplayName("Null email, Random Password")
        public void login_test_nullEmail(){

        }

        @Test
        @DisplayName("Valid email, Null password")
        public void login_test_nullPwd(){

        }

        @Test
        @DisplayName("Invalid email, Random Password")
        public void login_test_invalidEmail(){

        }

        @Test
        @DisplayName("Valid email, Invalid password")
        public void login_test_invalidPwd(){

        }

        @Test
        @DisplayName("Blank email, Random password")
        public void login_test_blankEmail(){

        }

        @Test
        @DisplayName("Valid email, Blank password")
        public void login_test_blankPwd(){

        }

    }

}
