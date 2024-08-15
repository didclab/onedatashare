package org.onedatashare.server.security.oauth2.user;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.onedatashare.server.security.oauth2.exceptions.OAuth2AuthenticationProcessingException;
import org.onedatashare.server.model.core.AuthProvider;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import java.io.IOException;
import java.util.*;


public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) throws IOException {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        if(registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.cilogon.toString())) {
            return new CilogonOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.github.toString())) {
            GitHub github = new GitHubBuilder().withOAuthToken(oAuth2UserRequest.getAccessToken().getTokenValue()).build();
            List<String> emails = github.getMyself().getEmails();
            Map<String, Object> newAttributes = new HashMap<>(attributes);
            if(!emails.isEmpty()) {
                newAttributes.put("externalEmail", emails.get(0));
            }
            else {
                newAttributes.put("externalEmail", null);
            }
            return new GithubOAuth2UserInfo(newAttributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
