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


package org.onedatashare.server.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HTTP file service ")
class HttpFileServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @BeforeAll
    // TODO: Initialize with a valid user
    public static void initialize(){

    }

    @AfterAll
    // TODO: Delete the user
    public static void cleanUp(){

    }

    @Test
    @DisplayName("testing list")
    public void listTest(){

    }

    @Test
    @DisplayName("testing download URL")
    public void getDownloadURLTest(){

    }

    @Test
    @DisplayName("testing submit")
    public void submitTest(){

    }

}