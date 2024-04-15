package org.onedatashare.server.security.oauth2.user;

import org.onedatashare.server.model.core.AuthProvider;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getFirstName() {
        String firstName = (String) attributes.get("name");
        if (firstName != null && firstName.contains(" ")) {
            firstName = firstName.substring(0, firstName.lastIndexOf(" "));
        }
        return firstName;
    }

    @Override
    public String getLastName() {
        String lastName = (String) attributes.get("name");
        if (lastName != null && lastName.contains(" ")) {
            lastName = lastName.substring(lastName.lastIndexOf(" ") + 1);
        }
        return lastName;
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
