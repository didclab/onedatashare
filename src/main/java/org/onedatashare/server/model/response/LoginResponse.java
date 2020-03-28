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


package org.onedatashare.server.model.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.server.model.core.User;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginResponse {
    private String email;
    private String token;
    private boolean saveOAuthTokens;
    private boolean isAdmin;
    private boolean compactViewEnabled;
    private long expiresIn;

    public static LoginResponse LoginResponseFromUser(User user, String token, long expiresIn){
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.token = token;
        loginResponse.email = user.getEmail();
        loginResponse.compactViewEnabled = user.isCompactViewEnabled();
        loginResponse.isAdmin = user.isAdmin();
        loginResponse.saveOAuthTokens = user.isSaveOAuthTokens();
        loginResponse.expiresIn = expiresIn;
        return loginResponse;
    }
}