package org.onedatashare.server.model.credential;

import lombok.Data;

import java.util.Date;

/**
 * POJO for storing OAuth Credentials
 */
@Data
public class OAuthEndpointCredential extends EndpointCredential {
    private String token;
    private boolean tokenExpires = false;
    private Date expiresAt;
    private String refreshToken;
    private boolean refreshTokenExpires = false;
}