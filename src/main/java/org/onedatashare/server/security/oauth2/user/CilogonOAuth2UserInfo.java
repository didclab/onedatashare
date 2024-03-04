package org.onedatashare.server.security.oauth2.user;

import java.util.Map;

public class CilogonOAuth2UserInfo extends OAuth2UserInfo {

    public CilogonOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }


    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getFirstName() {
        return (String) attributes.get("given_name");
    }

    @Override
    public String getLastName() {
        return (String) attributes.get("family_name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    @Override
    public String getOrganisation() {
        return (String) attributes.get("o");
    }

}
