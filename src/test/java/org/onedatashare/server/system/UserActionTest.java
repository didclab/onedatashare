package org.onedatashare.server.system;


import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.model.requestdata.UserRequestData;
import org.onedatashare.server.repository.UserRepository;
import org.onedatashare.server.service.CaptchaService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
public class UserActionTest {

    private static final String USER_CONTROLLER_URL = "/api/stork/user";
    private static final String TEST_USER_EMAIL = "bigstuff@bigwhop.com";
    private static final String TEST_USER_NAME = "test_user";

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
        when(userRepository.insert((User)any())).thenAnswer(addToUsers());
        when(userRepository.findById((String)any())).thenAnswer(getFromUsers());
        when(userRepository.save(any())).thenAnswer(updateUser());

        // Service mocked methods
        when(captchaService.verifyValue(any())).thenReturn(just(true));
        doAnswer(addToEmails()).when(emailService).sendEmail(any(), any(), any());
        doCallRealMethod().when(emailService).isValidEmail(any());

        users.clear();
        inbox.clear();
    }

    @Test
    public void givenUserDoesNotExist_WhenRegistered_ShouldBeAddedToUserRepository() throws Exception {
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals((getFirstUser()).getEmail(), TEST_USER_EMAIL);
    }

    @Test
    public void givenUserAlreadyExists_WhenRegistered_ShouldNotDuplicateUser() throws Exception {
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);
        long firstRegistrationTimestamp = getFirstUser().getRegisterMoment();

        // Try registering the same user again
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals(getFirstUser().getEmail(), TEST_USER_EMAIL);
        assertEquals(getFirstUser().getRegisterMoment(), firstRegistrationTimestamp);
    }

    @Test
    public void givenUserRegistered_WhenLoggingIn_ShouldSucceed() throws Exception {
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);
        String verificationCodeEmail = inbox.get(TEST_USER_EMAIL);
        String verificationCode = extractVerificationCode(verificationCodeEmail);

        verifyCode(TEST_USER_EMAIL, verificationCode);
        validateUser(TEST_USER_EMAIL, users.get(TEST_USER_EMAIL).getAuthToken());

        assertTrue(users.get(TEST_USER_EMAIL).isValidated());
    }

    private void verifyCode(String userEmail, String verificationCode) throws Exception {
        UserRequestData userRequestData = new UserRequestData();
        String verifyAction = "verifyCode";
        userRequestData.setAction(verifyAction);
        userRequestData.setEmail(userEmail);
        userRequestData.setCode(verificationCode);
        processWithUserAction(userRequestData);
    }

    private void validateUser(String userEmail, String authToken) throws Exception {
        UserRequestData userRequestData = new UserRequestData();
        String validateAction = "validate";
        userRequestData.setAction(validateAction);
        userRequestData.setEmail(userEmail);
        userRequestData.setCode(authToken);
        processWithUserAction(userRequestData);
    }

    private void registerUser(String userEmail, String firstName) throws Exception {
        UserRequestData userRequestData = new UserRequestData();
        String registerAction = "register";
        userRequestData.setAction(registerAction);
        userRequestData.setFirstName(firstName);
        userRequestData.setEmail(userEmail);
        processWithUserAction(userRequestData);
    }

    private String extractVerificationCode(String verificationCodeEmail) {
        return verificationCodeEmail.substring(verificationCodeEmail.lastIndexOf(" ")).trim();
    }

    private User getFirstUser() {
        return (User) users.values().toArray()[0];
    }

    private <T> T deserialize(String resp, Class<T> userClass) throws IOException {
        return new ObjectMapper().readValue(resp, userClass);
    }

    private void processWithUserAction(UserRequestData userRequestData) throws Exception {
        mvc.perform(post(USER_CONTROLLER_URL).content(toJson(userRequestData))
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String toJson(UserRequestData userRequestData) {
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
}
