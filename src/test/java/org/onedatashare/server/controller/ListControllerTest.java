package org.onedatashare.server.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.Resource;
import org.onedatashare.server.model.request.RequestData;
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
@WebMvcTest(value = ListController.class, secure = false)
public class ListControllerTest extends ControllerTest{

    private static final String LIST_CONTROLLER_URL = "/api/stork/ls";

    private List<ResourceService<?>> called = new ArrayList<>();

    @Before
    public void setup() {
        mockListMethodOf(dbxService);
        mockListMethodOf(vfsService);
        /* GridftpService does not implement ResourceService interface */
//        mockListMethodOf(gridService);
        mockListMethodOf(resourceService);
        mockListMethodOf(httpService);
    }

    @Test
    public void givenUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldRouteToCorrespondingService()
            throws Exception {
        for (ResourceService<?> service : authenticatingServices(supportedServices())) {
            String url = getServiceUri(service);
            processThenAssertServiceCalled(url);
        }
    }

    @Test
    public void givenUserCredentialAndUndefinedRequestType_WhenProcessed_ShouldNotRouteToAnyService()
            throws Exception {
        processRequest("Undefined_Type");
        assertTrue(called.isEmpty(),
                "Expected no services to be called for undefined request type, " +
                        "but the following services were called: "
                        + getClassNames(called));
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldReturnWithError()
            throws Exception {
        for (ResourceService<?> service : authenticatingServices(supportedServices())) {
            String uri = getServiceUri(service);
            RequestData requestData = nonCredentialedRequestDataOf(uri);
            processRequest(requestData)
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForNonAuthenticatingService_WhenProcessed_ShouldRouteToCorrespondingService()
            throws Exception {
        for (ResourceService<?> service : nonAuthenticatingServices(supportedServices())) {
            String uri = getServiceUri(service);
            RequestData requestData = nonCredentialedRequestDataOf(uri);
            processRequest(requestData);
            assertServiceCalled(getServiceClass(service), uri);
        }
    }

    private void mockListMethodOf(ResourceService<?> service) {
        Mockito.when(service.list(any(), any())).then(addToList(service));
    }

    private Answer<Mono<?>> addToList(ResourceService<?> service) {
        return v -> {
            called.add(service);
            return Mono.empty();
        };
    }

    private Stream<ResourceService<? extends Resource>> supportedServices() {
        return of(dbxService, vfsService, resourceService, httpService);
    }

    private void processRequest(String type) throws Exception {
        processRequest(credentialedRequestDataOf(type));
    }

    private ResultActions processRequest(RequestData requestData) throws Exception {
        return mvc.perform(jsonPostRequestOf(requestData, LIST_CONTROLLER_URL));
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