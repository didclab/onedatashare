/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.system;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.controller.LoginController.LoginControllerRequest;
import org.onedatashare.server.controller.RegistrationController.RegistrationControllerRequest;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.UserRepository;
import org.onedatashare.server.service.CaptchaService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

/**
 * A system test suite that tests actions on user accounts like logging in, changing passwords and verification.
 * In order to inform the backend components of the currently logged in user, each test must be annotated with the
 * {@link WithMockCustomUser} annotation, which is a custom annotation that extends and customizes the behavior
 * of Spring's security context. In order to emulate an anonymous request, Spring's {@link WithAnonymousUser}
 * should be used
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class UserActionTest {

    private static final String TEST_USER_EMAIL = "bigstuff@bigwhoopcorp.com";
    private static final String TEST_USER_NAME = "test_user";
    private static final String TEST_USER_PASSWORD = "IamTheWalrus";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CaptchaService captchaService;

    @MockBean
    private EmailService emailService;

    private Map<String, User> users = new HashMap<>();
    private Map<String, String> inbox = new HashMap<>();

    @Before
    public void setup() {

        // User repo mocked methods
        when(userRepository.insert((User) any())).thenAnswer(addToUsers());
        when(userRepository.findById(anyString())).thenAnswer(getFromUsers());
        when(userRepository.save(any())).thenAnswer(updateUser());

        // Service mocked methods
        when(captchaService.verifyValue(any())).thenReturn(just(true));
        doAnswer(addToEmails()).when(emailService).sendEmail(any(), any(), any());
        doCallRealMethod().when(emailService).isValidEmail(any());

        users.clear();
        inbox.clear();
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserDoesNotExist_WhenRegistered_ShouldBeAddedToUserRepository() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals((getFirstUser()).getEmail(), TEST_USER_EMAIL);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserAlreadyExists_WhenRegistered_ShouldNotDuplicateUser() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);
        long firstRegistrationTimestamp = getFirstUser().getRegisterMoment();

        // Try registering the same user again
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals(getFirstUser().getEmail(), TEST_USER_EMAIL);
        assertEquals(getFirstUser().getRegisterMoment(), firstRegistrationTimestamp);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserRegistered_WhenCodeIsVerified_ShouldValidateUser() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertTrue(users.get(TEST_USER_EMAIL).isValidated());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserVerified_WhenLoggingIn_ShouldSucceed() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        boolean wasSuccessful = loginUser(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        assertTrue(wasSuccessful);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserNotVerified_WhenLoggingIn_ShouldFail() throws Exception {
        // does not perform verification by email
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);

        boolean loginSuccessful = loginUser(TEST_USER_EMAIL, TEST_USER_PASSWORD);

        assertFalse(loginSuccessful);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserChangedPassword_WhenLoggingInWithNewPassword_ShouldSucceed() throws Exception {
        String oldPassword = TEST_USER_PASSWORD;
        String new_password = "new_password";
        registerUserAndChangePassword(TEST_USER_EMAIL, oldPassword, TEST_USER_NAME);
        loginUser(TEST_USER_EMAIL, oldPassword);

        resetUserPassword(TEST_USER_EMAIL, oldPassword, new_password);

        boolean loginSuccessful = loginUser(TEST_USER_EMAIL, new_password);
        assertTrue(loginSuccessful);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserChangedPassword_WhenLoggingInWithOldPassword_ShouldFail() throws Exception {
        String oldPassword = TEST_USER_PASSWORD;
        String new_password = "new_password";
        registerUserAndChangePassword(TEST_USER_EMAIL, oldPassword, TEST_USER_NAME);

        resetUserPassword(TEST_USER_EMAIL, oldPassword, new_password);

        boolean loginSuccessful = loginUser(TEST_USER_EMAIL, oldPassword);
        assertFalse(loginSuccessful);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenIncorrectOldPassword_WhenResettingPassword_ShouldFail() throws Exception {
        String oldPassword = TEST_USER_PASSWORD;
        String new_password = "new_password";
        String wrongPassword = "random_guess";
        registerUserAndChangePassword(TEST_USER_EMAIL, oldPassword, TEST_USER_NAME);

        resetUserPassword(TEST_USER_EMAIL, wrongPassword, new_password);

        boolean loginSuccessful = loginUser(TEST_USER_EMAIL, wrongPassword);
        assertFalse(loginSuccessful);
        loginSuccessful = loginUser(TEST_USER_EMAIL, oldPassword);
        assertTrue(loginSuccessful);
    }

    private void resetUserPassword(String userEmail, String oldPassword, String newPassword) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(oldPassword);
        requestData.setNewPassword(newPassword);
        requestData.setConfirmPassword(newPassword);
        processWithRequestData(ODSConstants.UPDATE_PASSWD_ENDPOINT, requestData);
    }

    private void setUserPassword(String userEmail, String userPassword, String authToken) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(userPassword);
        requestData.setConfirmPassword(userPassword);
        requestData.setCode(authToken);
        processWithRequestData(ODSConstants.RESET_PASSWD_ENDPOINT, requestData);
    }

    private boolean loginUser(String userEmail, String password) throws Exception {
        LoginControllerRequest requestData = new LoginControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setPassword(password);
        long timeBeforeLoggingIn = System.currentTimeMillis();
        processWithRequestData(ODSConstants.AUTH_ENDPOINT, requestData);
        Long lastActivity = users.get(userEmail).getLastActivity();
        return lastActivity != null && lastActivity > timeBeforeLoggingIn;
    }

    private void registerUserAndChangePassword(String userEmail, String userPassword, String username) throws Exception {
        registerUser(userEmail, username);
        String verificationCodeEmail = inbox.get(userEmail);
        String verificationCode = extractVerificationCode(verificationCodeEmail);
        verifyCode(userEmail, verificationCode);
        String authToken = users.get(userEmail).getAuthToken();
        setUserPassword(userEmail, userPassword, authToken);
    }

    private void verifyCode(String userEmail, String verificationCode) throws Exception {
        RegistrationControllerRequest requestData = new RegistrationControllerRequest();
        requestData.setEmail(userEmail);
        requestData.setCode(verificationCode);
        processWithRequestData(ODSConstants.EMAIL_VERIFICATION_ENDPOINT, requestData);
    }

    private void registerUser(String userEmail, String firstName) throws Exception {
        RegistrationControllerRequest requestData = new RegistrationControllerRequest();
        requestData.setFirstName(firstName);
        requestData.setEmail(userEmail);
        processWithRequestData(ODSConstants.REGISTRATION_ENDPOINT, requestData);
    }

    private String extractVerificationCode(String verificationCodeEmail) {
        return verificationCodeEmail.substring(verificationCodeEmail.lastIndexOf(" ")).trim();
    }

    private User getFirstUser() {
        return (User) users.values().toArray()[0];
    }

    private void processWithRequestData(String url, Object requestData) throws Exception {
        mvc.perform(post(url).with(csrf()).content(toJson(requestData))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String toJson(Object userRequestData) {
        return new Gson().toJson(userRequestData);
    }

    private Answer<Mono<User>> updateUser() {
        return invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getEmail(), user);
            return just(user);
        };
    }

    private Answer<?> addToEmails() {
        return invocation -> {
            String recipient = invocation.getArgument(0);
            String body = invocation.getArgument(2);
            inbox.put(recipient, body);
            return null;
        };
    }

    private Answer<Mono<User>> getFromUsers() {
        return invocation -> {
            User user = users.get(invocation.getArgument(0));
            return user != null ? just(user) : empty();
        };
    }

    private Answer<Mono<User>> addToUsers() {
        return invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getEmail(), user);
            return just(user);
        };
    }

    @SuppressWarnings("unused")
    private String encodeIntoCookie(Map<String, String> entries) {
        if (entries.isEmpty())
            return "";
        StringBuilder cookie = new StringBuilder();
        List<String> properties = new ArrayList<>();
        entries.forEach((key, value) -> properties.add(format("%s=%s", key, value)));
        cookie.append(properties.get(0));
        for (int i = 1; i < properties.size(); i++) {
            cookie.append(";").append(properties.get(i));
        }
        return cookie.toString();
    }
}
