package org.onedatashare.server.controller;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.requestdata.JobRequestData;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(CancelController.class)
public class CancelControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ResourceServiceImpl resourceService;

    private boolean wasCalled = false;
    private static final String CANCEL_CONTROLLER_URL = "/api/stork/cancel";

    @Before
    public void setup() {
        Mockito.when(resourceService.cancel(any(), any())).then(setWasCalled());
    }

    @NotNull
    private Answer<Mono<?>> setWasCalled() {
        return v -> {
            wasCalled = true;
            return Mono.empty();
        };
    }

    private String toJson(Object o) {
        return new Gson().toJson(o);
    }

    @Test
    public void givenCancelRequest_WhenProcessed_ShouldInvokeCancelMethod() throws Exception {
        mvc.perform(post(CANCEL_CONTROLLER_URL)
                .content(toJson(new JobRequestData()))
                .contentType(APPLICATION_JSON));
        assertTrue(wasCalled);
    }
}