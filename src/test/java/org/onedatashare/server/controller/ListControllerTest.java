package org.onedatashare.server.controller;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.requestdata.RequestData;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(ListController.class)
public class ListControllerTest {

    private static final String LIST_CONTROLLER_URL = "/api/stork/ls";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DbxService dbxService;

    @MockBean
    private VfsService vfsService;

    @MockBean
    private GridftpService gridService;

    @MockBean
    private ResourceServiceImpl resourceService;

    @MockBean
    private HttpFileService httpService;

    private List<? super ResourceService<?>> called = new ArrayList<>();

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
    public void givenValidUserCredentialAndValidRequestType_WhenProcessed_ShouldRouteToCorrespondingService() throws Exception {
        processThenAssertCalled(ODSConstants.DROPBOX_URI_SCHEME, DbxService.class);
        processThenAssertCalled(ODSConstants.DRIVE_URI_SCHEME, ResourceServiceImpl.class);
        processThenAssertCalled(ODSConstants.FTP_URI_SCHEME, VfsService.class);
        processThenAssertCalled(ODSConstants.HTTP_URI_SCHEME, HttpFileService.class);
//        processThenAssertCalled(ODSConstants.GRIDFTP_URI_SCHEME, GridftpService.class);
    }

    @Test
    public void givenValidUserCredentialAndUndefinedRequestType_WhenProcessed_ShouldNotRouteToAnyService()
            throws Exception {
        processRequest("Undefined_Type");
        assertTrue(called.isEmpty(),
                "Expected no services to be called for undefined request type, " +
                        "but the following services were called: "
                        + getClassNames(called));
    }

    @Test
    public void givenInvalidUserCredentialAndValidRequestType_WhenProcessed_ShouldReturnWithError()
            throws Exception {
        RequestData requestData = requestDataOf(ODSConstants.DROPBOX_URI_SCHEME);
        requestData.setCredential(null);
        mvc.perform(requestOf(requestData)).andDo(print())
                .andExpect(status().isInternalServerError());
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

    private RequestData requestDataOf(String type) {
        RequestData rd = new RequestData();
        rd.setCredential(new UserActionCredential());
        rd.setType(type);
        return rd;
    }

    private String toJson(Object o) {
        Gson gson = new Gson();
        String json = gson.toJson(o);
        return json;
    }

    private MockHttpServletRequestBuilder requestOf(RequestData requestData) {
        return post(LIST_CONTROLLER_URL)
                .content(toJson(requestData))
                .contentType(MediaType.APPLICATION_JSON);
    }

    private void processRequest(String type) throws Exception {
        RequestData requestData = requestDataOf(type);
        mvc.perform(requestOf(requestData)).andDo(print());
    }

    private List<String> getClassNames(List<?> l) {
        return l.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
    }

    private void processThenAssertCalled(String type, Class<? extends ResourceService<?>> clazz) throws Exception {
        processRequest(type);
        assertEquals(called.size(), 1,
                String.format("zero or more than one resource service was called for request type %s: %s",
                        type, getClassNames(called)));
        assertTrue(clazz.isInstance(called.get(0)),
                String.format("Expected controller to call %s, but %s was called",
                        clazz.getSimpleName(), called.get(0).getClass().getSimpleName()));
        called.clear();
    }
}