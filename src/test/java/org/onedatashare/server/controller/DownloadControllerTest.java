package org.onedatashare.server.controller;

import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.requestdata.RequestData;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.service.ResourceService;
import org.onedatashare.server.service.VfsService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.*;

@RunWith(SpringRunner.class)
@WebMvcTest(DownloadController.class)
public class DownloadControllerTest extends ControllerTest {

    private static final String DOWNLOAD_CONTROLLER_POST_URL = "/api/stork/download";
    private static final String DOWNLOAD_CONTROLLER_GET_URL = DOWNLOAD_CONTROLLER_POST_URL + "/file";

    private List<ResourceService<?>> called = new ArrayList<>();

    @Before
    public void setup(){
        called.clear();
        Mockito.when(dbxService.getDownloadURL(any(), any())).then(addToList(dbxService));
        Mockito.when(resourceService.download(any(), any())).then(addToList(resourceService));
        Mockito.when(boxService.download(any(), any())).then(addToList(boxService));
        Mockito.when(vfsService.getDownloadURL(any(), any())).then(addToList(vfsService));
        Mockito.when(vfsService.getSftpDownloadStream(any(), any())).then(addToList(vfsService));
    }

    @Test
    public void givenUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldCallCorrespondingService()
            throws Exception {
        List<? extends ResourceService<?>> authenticatingServices = asList(resourceService, boxService);
        for (ResourceService<?> service : authenticatingServices) {
            String uri = getServiceUri(service);
            processThenAssertServiceCalled(uri);
        }
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForAuthenticatingService_WhenProcessed_ShouldReturnInternalServerError()
            throws Exception {
        List<? extends ResourceService<?>> authenticatingServices = asList(resourceService, boxService);
        for (ResourceService<?> service : authenticatingServices) {
            String url = getServiceUri(service);
            RequestData requestData = nonCredentialedRequestDataOf(url);
            processThenAssertError(requestData);
        }
    }

    @Test
    public void givenNoUserCredentialAndRequestTypeForNonAuthenticatingService_WhenProcessed_ShouldCallCorrespondingService()
            throws Exception {
        List<? extends ResourceService<?>> nonAuthenticatingServices = asList(dbxService, vfsService);
        for (ResourceService<?> service : nonAuthenticatingServices) {
            String uri = getServiceUri(service);
            processRequest(nonCredentialedRequestDataOf(uri));
            assertServiceCalled(getServiceClass(service), uri);
        }
    }

    @Test
    public void givenUserCredentialAndRequestTypeForUnknownService_WhenProcessed_ShouldNotCallAnyService()
            throws Exception {
        processRequest(nonCredentialedRequestDataOf("Undefined_type"));
        assertTrue(called.isEmpty(),
                "Expected no services to be called for undefined request type, " +
                        "but the following services were called: "
                        + getClassNames(called));
    }

    @Test
    public void givenValidCookie_WhenGetRequestIsProcessed_ShouldCallCorrespondingService() throws Exception {
        String cookieName = "CX";
        UserActionResource actionResource = new UserActionResource();
        MockHttpServletRequestBuilder request = getRequestOf(DOWNLOAD_CONTROLLER_GET_URL)
                .header(ODSConstants.COOKIE, encodeIntoCookie(cookieName, actionResource));
        mvc.perform(request);
        assertServiceCalled(VfsService.class, DOWNLOAD_CONTROLLER_GET_URL);
    }

    @Test
    public void givenMissingCookie_WhenGetRequestIsProcessed_ShouldThrowException() {
        MockHttpServletRequestBuilder request = getRequestOf(DOWNLOAD_CONTROLLER_GET_URL)
                .header(ODSConstants.COOKIE, "");
        assertThrows(NestedServletException.class, () -> mvc.perform(request));
    }

    @NotNull
    private String encodeIntoCookie(String cookieName, Object cookieValue) {
        String valueAsJson = toJson(cookieValue);
        return ServerCookieEncoder.LAX.encode(cookieName, valueAsJson);
    }

    private void processThenAssertError(RequestData request) throws Exception {
        processRequest(request).andExpect(status().isInternalServerError());
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
        return mvc.perform(jsonPostRequestOf(requestData, DOWNLOAD_CONTROLLER_POST_URL));
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
                String.format("Expected controller to call %s for request type %s, but %s was called",
                        serviceClass.getSimpleName(), type, called.get(0).getClass().getSimpleName()));
        called.clear();
    }
}