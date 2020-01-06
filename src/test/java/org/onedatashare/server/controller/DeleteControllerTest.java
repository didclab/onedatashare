package org.onedatashare.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.requestdata.RequestData;
import org.onedatashare.server.service.ResourceService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(DeleteController.class)
public class DeleteControllerTest extends ControllerTest {

    private static final String DELETE_CONTROLLER_URL = "/api/stork/delete";

    private List<ResourceService<?>> called = new ArrayList<>();

    @Before
    public void setup(){
        mockDeleteMethodOf(dbxService);
        mockDeleteMethodOf(vfsService);
        mockDeleteMethodOf(resourceService);
    }

    @Test
    public void givenUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldCallCorrespondingService() throws Exception {
        for (ResourceService<?> service : authenticatingServices(supportedServices())) {
            String uri = getServiceUri(service);
            processThenAssertServiceCalled(uri);
        }
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldReturnInternalServerError()
            throws Exception {
        for (ResourceService<?> service : authenticatingServices(supportedServices())) {
            String url = getServiceUri(service);
            RequestData requestData = nonCredentialedRequestDataOf(url);
            processThenAssertError(requestData);
        }
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForNonAuthenticatingService_WhenProcessed_ShouldCallCorrespondingService() throws Exception {
        for (ResourceService<?> service : nonAuthenticatingServices(supportedServices())) {
            String uri = getServiceUri(service);
            processRequest(nonCredentialedRequestDataOf(uri));
            assertServiceCalled(getServiceClass(service), uri);
        }
    }

    private void processThenAssertError(RequestData request) throws Exception {
        processRequest(request).andExpect(status().isInternalServerError());
    }

    private Stream<ResourceService<? extends Resource>> supportedServices() {
        return of(dbxService, vfsService, resourceService);
    }

    private void mockDeleteMethodOf(ResourceService<?> service) {
        Mockito.when(service.delete(any(), any())).then(addToList(service));
    }

    private Answer<Mono<?>> addToList(ResourceService<?> service) {
        return v -> {
            called.add(service);
            return Mono.empty();
        };
    }

    private void processRequest(String type) throws Exception {
        RequestData requestData = credentialedRequestDataOf(type);
        requestData.setUri(type);
        processRequest(requestData);
    }

    private ResultActions processRequest(RequestData requestData) throws Exception {
        return mvc.perform(jsonPostRequestOf(requestData, DELETE_CONTROLLER_URL));
    }

    private void processThenAssertServiceCalled(String type) throws Exception {
        processRequest(type);
        Class<? extends ResourceService> serviceClass = getUriService(type);
        assertServiceCalled(serviceClass, type);
    }

    private void assertServiceCalled(Class<? extends ResourceService> serviceClass, String type) {
        assertEquals(called.size(), 1,
                String.format("zero or more than one resource service was called for request type %s: %s",
                        type, getClassNames(called)));
        assertTrue(serviceClass.isInstance(called.get(0)),
                String.format("Expected controller to call %s, but %s was called",
                        serviceClass.getSimpleName(), called.get(0).getClass().getSimpleName()));
        called.clear();
    }
}