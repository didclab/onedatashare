package org.onedatashare.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.jobaction.JobRequest;
import org.onedatashare.server.service.JobService;
import org.onedatashare.server.service.ResourceService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(QueueController.class)
public class QueueControllerTest extends ControllerTest {

    private static final String QUEUE_CONTROLLER_URL = "/api/stork/q";
    public static final String UPDATE_SUB_URL = "/update";

    private List<JobService> called = new ArrayList<>();

    @Before
    public void setup() {
        when(jobService.getJobsForUserOrAdmin(any(), any())).then(addToList(jobService));
        when(jobService.getUpdates(any(), any())).then(addToList(jobService));
    }

    @Test
    public void givenJobRequest_WhenQueueIsRequested_ShouldCallGetQueueMethodOfService() throws Exception {
        processQueueRequest(new JobRequest());
        assertServiceCalled(JobService.class);
    }

    @Test
    public void givenListOfJobIds_WhenUpdatesAreRequested_ShouldReturnJobUpdates() throws Exception {
        processUpdateRequest(randomIdsListOfSize(3));
        assertServiceCalled(JobService.class);
    }

    private List<? extends UUID> randomIdsListOfSize(int size) {
        List<UUID> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    private Answer<Mono<?>> addToList(JobService service) {
        return v -> {
            called.add(service);
            return Mono.empty();
        };
    }

    private ResultActions processQueueRequest(JobRequest requestData) throws Exception {
        return mvc.perform(jsonPostRequestOf(requestData, QUEUE_CONTROLLER_URL));
    }

    private ResultActions processUpdateRequest(List<? extends UUID> requestData) throws Exception {
        return mvc.perform(jsonPostRequestOf(requestData, QUEUE_CONTROLLER_URL + UPDATE_SUB_URL));
    }

    private void assertServiceCalled(Class<?> serviceClass) {
        assertEquals(called.size(), 1,
                String.format("zero or more than one resource service was called for request: %s", getClassNames(called)));
        assertTrue(serviceClass.isInstance(called.get(0)),
                String.format("Expected controller to call %s, but %s was called",
                        serviceClass.getSimpleName(), called.get(0).getClass().getSimpleName()));
        called.clear();
    }
}