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


package org.onedatashare.server.model.credential;

import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import java.util.Date;

@Data
public class OAuthCredential extends Credential {
  public transient String token;
  public String name;
  public String dropboxID;
  public String refreshToken;
  public Date expiredTime;
  public boolean refreshTokenExp = false;
  public Date lastRefresh;

  public OAuthCredential(String token) {
    this.type = CredentialType.OAUTH;
    this.token = token;

  }
}
