package org.onedatashare.server.system;

import com.google.gson.Gson;
import org.junit.Before;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.UserRepository;
import org.onedatashare.server.service.CaptchaService;
import org.onedatashare.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static reactor.core.publisher.Mono.empty;
import static reactor.core.publisher.Mono.just;

public abstract class SystemTest {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserRepository userRepository;

    @MockBean
    private CaptchaService captchaService;

    @MockBean
    protected EmailService emailService;

    protected static final String TEST_USER_EMAIL = "bigstuff@bigwhoopcorp.com";
    protected static final String TEST_USER_NAME = "test_user";
    protected static final String TEST_USER_PASSWORD = "IamTheWalrus";

    protected Map<String, User> users = new HashMap<>();
    protected Map<String, String> userInbox = new HashMap<>();

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
        userInbox.clear();
    }

    protected User getFirstUser() {
        return (User) users.values().toArray()[0];
    }

    protected ResultActions processPostWithRequestData(String url, Object requestData) throws Exception {
        return processWithRequestData(requestData, post(url));
    }

    protected ResultActions processPutWithRequestData(String url, Object requestData) throws Exception {
        return processWithRequestData(requestData, put(url));
    }

    protected ResultActions processGetWithRequestData(String url, Object requestData) throws Exception {
        return processWithRequestData(requestData, get(url));
    }

    protected ResultActions processGetWithNoRequestData(String url) throws Exception {
        return processWithNoRequestData(get(url));
    }

    private ResultActions processWithRequestData(Object requestData,
                                                      MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.with(csrf()).content(toJson(requestData))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    private ResultActions processWithNoRequestData(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.with(csrf())).andDo(print());
    }

    /**
     * To be used when the object has already been converted to json
     * This is provided if the default json conversion needs to be overriden in cases where
     * the fields have a different json field name than that in the class variable declaration
     *
     * @param url url to route to
     * @param requestData json representation of the request data
     * @return result of executing the mvc request
     */
    protected ResultActions processPostWithRequestData(String url, String requestData) throws Exception {
        return processWithRequestData(requestData, post(url));
    }

    private ResultActions processWithRequestData(String requestData,
                                                   MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.with(csrf()).content(requestData)
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
    }

    protected <T> T getMvcResult(MvcResult mvcResult, Class<T> resultType) throws Exception {
        MvcResult asyncResult = mvc.perform(asyncDispatch(mvcResult))
                .andReturn();
        String contentAsString = asyncResult.getResponse().getContentAsString();
        return fromJson(contentAsString, resultType);
    }

    protected <T> T fromJson(String json, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    protected String toJson(Object userRequestData) {
        return new Gson().toJson(userRequestData);
    }

    private Answer<Mono<User>> updateUser() {
        return invocation -> {
            User user = invocation.getArgument(0);
            users.put(user.getEmail(), user);
            return just(user);
        };
    }

    protected Answer<?> addToEmails() {
        return invocation -> {
            String recipient = invocation.getArgument(0);
            String body = invocation.getArgument(2);
            userInbox.put(recipient, body);
            return null;
        };
    }

    protected Answer<Mono<User>> getFromUsers() {
        return invocation -> {
            User user = users.get(invocation.getArgument(0));
            return user != null ? just(user) : empty();
        };
    }

    protected Answer<Mono<User>> addToUsers() {
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
