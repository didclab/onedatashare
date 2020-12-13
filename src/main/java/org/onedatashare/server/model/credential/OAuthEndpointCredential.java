package org.onedatashare.server.model.credential;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * POJO for storing OAuth Credentials
 */
@Data
@Accessors(chain = true)
public class OAuthEndpointCredential extends EndpointCredential {
    private String token;
    private boolean tokenExpires = false;
    private Date expiresAt;
    private String refreshToken;
    private boolean refreshTokenExpires = false;

    public OAuthEndpointCredential(String id){
        super(id);
    }

}