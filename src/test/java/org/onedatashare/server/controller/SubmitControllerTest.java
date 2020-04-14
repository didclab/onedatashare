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
import org.onedatashare.server.model.request.JobRequestData;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertTrue;

@RunWith(SpringRunner.class)
@WebMvcTest(value = SubmitController.class, secure = false)
public class SubmitControllerTest extends ControllerTest {

    private boolean wasCalled = false;
    private static final String SUBMIT_CONTROLLER_URL = "/api/stork/submit";

    @Before
    public void setup() {
        Mockito.when(resourceService.submit(any(), any())).then(setWasCalled());
    }

    @NotNull
    private Answer<Mono<?>> setWasCalled() {
        return v -> {
            wasCalled = true;
            return Mono.empty();
        };
    }

    @Test
    public void givenSubmitRequest_WhenProcessed_ShouldInvokeSubmitJobMethod() throws Exception {
        JobRequestData requestData = new JobRequestData();
        mvc.perform(jsonPostRequestOf(requestData, SUBMIT_CONTROLLER_URL));
        assertTrue(wasCalled);
    }
}