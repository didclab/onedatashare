package org.onedatashare.server.security.oauth2.user;

import org.onedatashare.server.model.core.AuthProvider;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("firstName");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("secondName");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("externalEmail");
    }
    @Override
    public String getOrganisation() {
        return String.valueOf(AuthProvider.github);
    }

}
