/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.controller;

import com.google.gson.Gson;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.of;
import static org.onedatashare.server.model.core.ODSConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

abstract class ControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    DbxService dbxService;

    @MockBean
    VfsService vfsService;

    @MockBean
    GridFtpService gridService;

    @MockBean
    GDriveService gDriveService;

    @MockBean
    ResourceServiceImpl resourceService;

    @MockBean
    HttpFileService httpService;

    @MockBean
    JobService jobService;

    @MockBean
    BoxService boxService;

    /**
     * Classes of the services that require user authentication in order to process the request
     */
    private static final Set<Class<? extends ResourceService>> authenticatingServices = unmodifiableSet(
            of(DbxService.class, GDriveService.class).collect(toSet()));

    /**
     * Maps each {@link ResourceService} to the uri that it handles
     */
    private static Map<Class<? extends ResourceService>, String> serviceUri = unmodifiableMap(
            new HashMap<Class<? extends ResourceService>, String>(){{
                put(DbxService.class, DROPBOX_URI_SCHEME);
                put(GDriveService.class, GDRIVE_URI_SCHEME);
                put(HttpFileService.class, HTTP_URI_SCHEME);
                put(VfsService.class, FTP_URI_SCHEME);
//                put(BoxService.class, BOX_URI_SCHEME);
            }});

    /**
     * Maps a uri to the {@link ResourceService} that it is linked to
     */
    private static final Map<String, Class<? extends ResourceService>> uriService = serviceUri.entrySet()
            .stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey));

    static String getServiceUri(ResourceService service) {
        return serviceUri.get(getServiceClass(service));
    }

    static Class<? extends ResourceService> getUriService(String uri) {
        return uriService.get(uri);
    }

    @SuppressWarnings("unchecked")
    static Class<? extends ResourceService> getServiceClass(ResourceService service) {
        return (Class<? extends ResourceService>) service.getClass().getSuperclass();
    }

    static RequestData credentialedRequestDataOf(String type) {
        RequestData rd = new RequestData();
        rd.setCredential(new UserActionCredential());
        rd.setType(type);
        return rd;
    }

    static RequestData nonCredentialedRequestDataOf(String url) {
        RequestData requestData = credentialedRequestDataOf(url);
        requestData.setUri(url);
        requestData.setCredential(null);
        return requestData;
    }

    static String toJson(Object o) {
        return new Gson().toJson(o);
    }

    static List<String> getClassNames(List<?> l) {
        return l.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .collect(toList());
    }

    static MockHttpServletRequestBuilder jsonPostRequestOf(RequestData requestData, String url) {
        return jsonPostRequestOf((Object)requestData, url);
    }

    static MockHttpServletRequestBuilder jsonPostRequestOf(Object requestData, String url) {
        return jsonRequestOf(post(url), requestData);
    }

    static MockHttpServletRequestBuilder getRequestOf(String url) {
        return get(url);
    }

    private static MockHttpServletRequestBuilder jsonRequestOf(MockHttpServletRequestBuilder request, Object requestData) {
        return request
                .content(toJson(requestData))
                .contentType(MediaType.APPLICATION_JSON);
    }

    Set<? extends ResourceService> authenticatingServices(Stream<? extends ResourceService> services) {
        return services
                .filter(service -> authenticatingServices.contains(getServiceClass(service)))
                .collect(toSet());
    }

    Set<? extends ResourceService> nonAuthenticatingServices(Stream<? extends ResourceService> services) {
        return services
                .filter(service -> !authenticatingServices.contains(getServiceClass(service)))
                .collect(toSet());
    }
}
