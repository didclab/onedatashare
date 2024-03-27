package org.onedatashare.server.security.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("application-prod.properties")
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class OAuthClientProperties {
    @Autowired
    private Environment env;

    public String getClientId(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".client-id");
    }

    public String getClientSecret(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".client-secret");
    }

    public String getRedirectUriTemplate(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".redirect-uri-template");
    }

    public String getAuthorizationUri(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".authorization-uri");
    }

    public String getTokenUri(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".token-uri");
    }

    public String getUserinfoUri(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".userinfo-uri");
    }

    public String getJwkSetUri(String providerType) {
        return env.getProperty("spring.security.oauth2.client.registration." + providerType + ".jwk-set-uri");
    }
}
