package org.onedatashare.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.core.User.UserLogin;
import org.onedatashare.server.model.response.LoginResponse;
import org.onedatashare.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {
	
    String validInputEmail = "ods_test_user@test.com";
    String invalidInputEmail = "nonexistent_ods_test_user@test.com";
    String testCookies = "email=ods_test_user@test.com;hash=19ab9f8ccaf5d80e6c55b17105591443d85d30bc54e905cc7a83e027b154cfee;saveOAuthTokens=false;compactViewEnabled=false";
    String testCookiesInvalid = "email="+invalidInputEmail+";hash=19ab9f8ccaf5d80e6c55b17105591443d85d30bc54e905cc7a83e027b154cfee;saveOAuthTokens=false;compactViewEnabled=false";


    @MockBean
    UserRepository userRepository;

    @Autowired
    private UserService userService;

    User user;

   // String validInputEmail = "ods_test_user@test.com";
   // String invalidInputEmail = "Invalid_ods_test_user@test.com";


    @BeforeEach
    public void initTest(){
        user = new User("ods_test_user@test.com","password");
        user.setHash("19ab9f8ccaf5d80e6c55b17105591443d85d30bc54e905cc7a83e027b154cfee");
        user.setFirstName("ODS_Test");
        user.setLastName("User");
        user.setAdmin(false);
        
        when(userRepository.findById(user.getEmail())).thenReturn(Mono.just(user));
        when(userRepository.findById(invalidInputEmail)).thenReturn(Mono.empty());
        when(userRepository.findById("")).thenReturn(Mono.empty());
        doThrow(IllegalArgumentException.class).when(userRepository).findById((String) null);
        
    }
    
    @Test
    @DisplayName("UserService.createUser()")
    // Check the test cases
    public void createUserTest() {

    	// No email and password provided
        assertEquals("Invalid email address.",assertThrows(RuntimeException.class, () -> {
                    userService.createUser(new User("", "")).subscribe();
                }, "allows creation of user with no email and password provided"
        ).getMessage());
        
        // Only password provided
        assertEquals("Invalid email address.",assertThrows(RuntimeException.class, () -> {
                    userService.createUser(new User("", "abcdefgh")).subscribe();
                }, "allows creation of user with no email provided"
        ).getMessage());
        
        // Only email provided
        assertEquals("No password was provided.",assertThrows(RuntimeException.class, () -> {
                    userService.createUser(new User("a@e.com", "")).subscribe();
                }, "allows creation of user with no password provided"
        ).getMessage());
        
        // Short password provided
        assertEquals("Password must be "+User.PASS_LEN+"+ characters.",assertThrows(RuntimeException.class, () -> {
                    userService.createUser(new User("a@e.com", "abcd")).subscribe();
                }, "allows creation of user with short password"
        ).getMessage());

        // Invalid email format provided
        assertEquals("Invalid email address.",assertThrows(RuntimeException.class, () -> {
                    userService.createUser(new User("invalidEmail", "abcdefgh")).subscribe();
                }, "allows creation of user with invalid email format"
        ).getMessage());
        
    }
    
    @Nested
    @DisplayName("getUser()")
    class GetUserTest{

        @Test
        @DisplayName("Valid user email")
        public void getUser_test_validEmail(){
            userService.getUser(validInputEmail)
                    .doOnSuccess(user ->{
                    	assertTrue(user instanceof User, "Expected of receive a User object");
                        assertEquals(validInputEmail, user.getEmail(),"Did not retrieve the expected user");
                    }).subscribe();
        }

        @Test
        @DisplayName("Invalid user email")
        public void getUser_test_invalidEmail(){
            userService.getUser(invalidInputEmail)
                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
                    .doOnError(error -> {
                        assertTrue(error instanceof Exception, "Did not receive expected error");
                        assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail,"Did not retrieve the expected user");
                    }).subscribe();
        }

        @Test
        @DisplayName("Blank user email")
        public void getUser_test_blankEmail(){
            userService.getUser("")
                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
                    .doOnError(error -> {
                        assertTrue(error instanceof Exception, "Did not receive expected error");
                        assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail,"Expected error not encountered");
                    }).subscribe();
        }


        // Test case not handled in original code. Need to fix!!!!!!
//        @Test
//        @DisplayName("Null user email input")
//        public void getUser_test_nullEmail(){
//            userService.getUser(null)
//                    .doOnSuccess(user -> fail("Did not expect to retrieve any user"))
//                    .doOnError(error -> {
//                    	System.out.println("Inside doOnError------------");
//                        assertTrue(error instanceof Exception, "Did not receive expected error");
//                        assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail,"Expected error not encountered");
//                    }).subscribe();
//        }
    }
    @Test
    @DisplayName("Valid email, Valid Password")
    public void login_test_Email(){
    	userService.login(validInputEmail, "RandomPassword").doOnSuccess(userLogin ->{
    		assertTrue(userLogin instanceof LoginResponse, "Expected of receive a UserLogin object");
    		assertEquals(validInputEmail, userLogin.getEmail(),"Did not retrieve the expected user");
    	}).subscribe();
    }

    @Test
    @DisplayName("valid cookie contains valid email")
    public void getLoggedInUserTest() {
        userService.getLoggedInUser(testCookies).doOnSuccess(user -> {
            assertEquals(validInputEmail, user.getEmail(), "Did not retrieve the expected user");
        }).subscribe();
    }

    @Test
    @DisplayName("valid cookie contains Invalid email")
    public void getLoggedInUserTestWithIncorrectEmail() {
        userService.getLoggedInUser(testCookiesInvalid).doOnError(error -> {
            assertTrue(error instanceof Exception, "Did not receive expected error");
            assertEquals(error.getMessage(), "No User found with Id: " + invalidInputEmail, "Did not retrieve the expected user");
        }).subscribe();
    }
}
