package org.onedatashare.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.requestdata.JobRequestData;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;


@RunWith(SpringRunner.class)
@WebMvcTest(DeleteJobController.class)
public class DeleteJobControllerTest extends ControllerTest {

    private boolean wasCalled = false;
    private static final String DELETE_JOB_CONTROLLER_URL = "/api/stork/deleteJob";

    @Before
    public void setup() {
        when(resourceService.deleteJob(any(), any())).then(setWasCalled());
    }

    @Test
    public void givenDeleteJobRequest_WhenProcessed_ShouldInvokeDeleteJobMethod() throws Exception {
        JobRequestData requestData = new JobRequestData();
        mvc.perform(jsonPostRequestOf(requestData, DELETE_JOB_CONTROLLER_URL));
        assertTrue(wasCalled,
                String.format(
                        "Expected service %s to be called on deleteJob request, but it was not called",
                        getServiceClass(resourceService).getSimpleName()
                ));
    }

    private Answer<Mono<?>> setWasCalled() {
        return v -> {
            wasCalled = true;
            return Mono.empty();
        };
    }
}