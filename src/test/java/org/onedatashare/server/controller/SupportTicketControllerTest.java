package org.onedatashare.server.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.request.JobRequestData;
import org.onedatashare.server.service.SupportTicketService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(value = SupportTicketController.class, secure = false)
public class SupportTicketControllerTest extends ControllerTest {

    private boolean wasCalled = false;
    private static final String CREATE_TICKET_CONTROLLER_URL = "/api/stork/ticket";

    @MockBean
    private SupportTicketService supportTicketService;

    @Before
    public void setup() {
        Mockito.when(supportTicketService.createSupportTicket(any())).then(setWasCalled());
    }

    @NotNull
    private Answer<Mono<?>> setWasCalled() {
        return v -> {
            wasCalled = true;
            return Mono.empty();
        };
    }

    @Test
    public void givenCreateTicketRequest_WhenProcessed_ShouldInvokeCreateTicketMethod() throws Exception {
        JobRequestData requestData = new JobRequestData();
        mvc.perform(jsonPostRequestOf(requestData, CREATE_TICKET_CONTROLLER_URL));
        assertTrue(wasCalled);
    }
}