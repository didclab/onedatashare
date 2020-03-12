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

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CredController.class, secure = false)
public class CredControllerTest extends ControllerTest {

    private static final String GET_CREDS_URL = "/api/stork/cred";
    private static final String SAVE_CREDS_URL = GET_CREDS_URL + "/saveCredentials";
    private boolean wasCalled = false;

    @MockBean
    private UserService userService;

    @Before
    public void setup() {
        wasCalled = false;
        resetMockedMethods();
    }

    @NotNull
    private Answer<Mono<?>> setWasCalled() {
        return v -> {
            wasCalled = true;
            return Mono.empty();
        };
    }

    @Test
    public void givenListCredsGetRequest_WhenProcessed_ShouldInvokeGetCredsMethod() throws Exception {
        mockGetCredsMethod();
        mvc.perform(getRequestOf(GET_CREDS_URL));
        assertTrue(wasCalled);
    }

    @Test
    public void givenSaveCredsPostRequest_WhenProcessed_ShouldInvokeSaveCredsMethod() throws Exception {
        mockSaveCredsMethods();
        OAuthCredential validCred = new OAuthCredential("token");
        List<OAuthCredential> creds = singletonList(validCred);
        mvc.perform(jsonPostRequestOf(creds, SAVE_CREDS_URL));
        assertTrue(wasCalled);
    }

    private void mockSaveCredsMethods() {
        Mockito.when(userService.saveUserCredentials(any(), any())).then(setWasCalled());
    }

    private void mockGetCredsMethod() {
        Mockito.when(userService.getCredentials(any())).then(setWasCalled());
    }

    private void resetMockedMethods() {
        Mockito.when(userService.getCredentials(any())).then(v -> Mono.empty());
        Mockito.when(userService.saveUserCredentials(any(), any())).then(v -> Mono.empty());
    }
}