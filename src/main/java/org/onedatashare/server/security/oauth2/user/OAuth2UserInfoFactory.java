package org.onedatashare.server.security.oauth2.user;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.onedatashare.server.security.oauth2.exceptions.OAuth2AuthenticationProcessingException;
import org.onedatashare.server.model.core.AuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;


public class OAuth2UserInfoFactory {

    @Value("${github.organization.id}")
    static String organizationId;

    public static OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) throws IOException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        if(registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.cilogon.toString())) {
            return new CilogonOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.github.toString())) {
            GitHub github = new GitHubBuilder().withOAuthToken(oAuth2UserRequest.getAccessToken().getTokenValue(), organizationId).build();
            if(Objects.nonNull(github.getMyself().getEmail())) {
                attributes.put("externalEmail", github.getMyself().getEmail());
            }
            else {
                attributes.put("externalEmail", null);
            }
            return new GithubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
