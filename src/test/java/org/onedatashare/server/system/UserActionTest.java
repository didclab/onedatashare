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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;
import static reactor.core.publisher.Mono.just;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserActionTest {

    private static final String USER_CONTROLLER_URL = "/api/stork/user";
    public static final String TEST_USER_EMAIL = "bigstuff@bigwhop.com";
    public static final String TEST_USER_NAME = "test_user";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CaptchaService captchaService;

    private Map<String, User> users = new HashMap<>();

    @Before
    public void setup() {
        when(userRepository.insert((User)any())).thenAnswer(addToUsers());
        when(userRepository.findById((String)any())).thenAnswer(getFromUsers());
        when(captchaService.verifyValue(any())).thenReturn(just(true));
        users.clear();
    }

    private Answer<Mono<User>> getFromUsers() {
        return invocation -> {
            User user = users.get(invocation.getArgument(0));
            return user != null ? just(user) : Mono.empty();
        };
    }

    private Answer<Mono<User>> addToUsers() {
        return invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getEmail(), user);
            return just(user);
        };
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

    private User getFirstUser() {
        return (User) users.values().toArray()[0];
    }

    private Mono<User> registerUser(String userEmail, String firstName) throws Exception {
        UserRequestData userRequestData = new UserRequestData();
        String registerAction = "register";
        String testUserName = firstName;

        userRequestData.setAction(registerAction);
        userRequestData.setFirstName(firstName);
        userRequestData.setEmail(userEmail);

        processWithUserAction(userRequestData);
        return null;
    }

    private <T> T deserialize(String resp, Class<T> userClass) throws IOException {
        return new ObjectMapper().readValue(resp, userClass);
    }

    private String processWithUserAction(UserRequestData userRequestData) throws Exception {
        return mvc.perform(post(USER_CONTROLLER_URL).content(toJson(userRequestData))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String toJson(UserRequestData userRequestData) {
        return new Gson().toJson(userRequestData);
    }
}
