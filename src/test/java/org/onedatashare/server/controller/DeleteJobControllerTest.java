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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.onedatashare.server.model.request.JobRequestData;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;


@RunWith(SpringRunner.class)
@WebMvcTest(value = DeleteJobController.class, secure = false)
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