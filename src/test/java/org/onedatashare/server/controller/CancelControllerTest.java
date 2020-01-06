package org.onedatashare.server.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.requestdata.JobRequestData;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(CancelController.class)
public class CancelControllerTest extends ControllerTest {

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

    @Test
    public void givenCancelRequest_WhenProcessed_ShouldInvokeCancelMethod() throws Exception {
        JobRequestData requestData = new JobRequestData();
        mvc.perform(jsonPostRequestOf(requestData, CANCEL_CONTROLLER_URL));
        assertTrue(wasCalled);
    }
}