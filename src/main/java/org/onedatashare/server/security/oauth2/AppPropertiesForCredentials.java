package org.onedatashare.server.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class AppPropertiesForCredentials {

    private final Map<String, OAuth2ClientProperties> clients = new HashMap<>();

    public Map<String, OAuth2ClientProperties> getClients() {
        return clients;
    }

    public static class OAuth2ClientProperties {
        private String clientId;
        private String clientSecret;
        private String authorizationUri;
        private String tokenUri;
        private String userinfoUri;
        private String jwtSetUri;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        public String getAuthorizationUri() {
            return authorizationUri;
        }

        public void setAuthorizationUri(String authorizationUri) {
            this.authorizationUri = authorizationUri;
        }
        public String getTokenUri() {
            return tokenUri;
        }

        public void setTokenUri(String tokenUri) {
            this.tokenUri = tokenUri;
        }
        public String getUserinfoUri() {
            return userinfoUri;
        }

        public void setUserinfoUri(String userinfoUri) {
            this.userinfoUri = userinfoUri;
        }
        public String getJwtSetUri() {
            return jwtSetUri;
        }

        public void setJwtSetUri(String jwtSetUri) {
            this.jwtSetUri = jwtSetUri;
        }
    }
}

