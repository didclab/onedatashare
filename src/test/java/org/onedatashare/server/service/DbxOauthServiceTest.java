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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.onedatashare.server.service.oauth.DbxOauthService;
import org.springframework.beans.factory.annotation.Autowired;

public class DbxOauthServiceTest {

    @Autowired
    private DbxOauthService dbxOauthService;

    @Test
    @DisplayName("API access key and secret key")
    @Disabled
    public void apiKeys(){
        assertTrue(dbxOauthService.keysNotNull(), "Missing access/secret key");
    }

    @Test
    public void start_givenNothing_throwsRuntimeException() {
    }
}