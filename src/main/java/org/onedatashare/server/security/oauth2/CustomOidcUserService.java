package org.onedatashare.server.security.oauth2;

import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.onedatashare.server.repository.UserRepository;
import org.onedatashare.server.security.oauth2.exceptions.OAuth2AuthenticationProcessingException;
import org.onedatashare.server.security.oauth2.exceptions.UserAlreadyExistException;
import org.onedatashare.server.security.oauth2.user.OAuth2UserInfo;
import org.onedatashare.server.security.oauth2.user.OAuth2UserInfoFactory;
import org.onedatashare.server.security.oauth2.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomOidcUserService extends org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService {

    @Autowired
    private UserRepository userRepository;

    private List<Role> roles;

    @Override
    public OidcUser loadUser(OidcUserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OidcUser oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOAuth2User(OidcUserRequest oAuth2UserRequest, OidcUser oAuth2User) throws IOException {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, oAuth2User.getAttributes());

        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findById(oAuth2UserInfo.getEmail());
        if(userOptional.isPresent() && userOptional.get().getHash() != null) {
            throw new UserAlreadyExistException("Login with your existing credentials");
        }
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getOrganization().equals(oAuth2UserInfo.getOrganisation())) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with an another account. Please use correct account to login.");
            }
            user = updateExistingUser(user);
        }
        else {
            user = registerNewUser(oAuth2UserInfo);
        }
        OidcUser oidcUser = (OidcUser) oAuth2User;
        return UserPrincipal.create(user, oAuth2User.getAttributes(), oidcUser.getIdToken());
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setFirstName(oAuth2UserInfo.getFirstName());
        user.setLastName(oAuth2UserInfo.getLastName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setOrganization(oAuth2UserInfo.getOrganisation());
        user.setRegisterMoment(System.currentTimeMillis());
        user.setLastActivity(System.currentTimeMillis());
        user.setValidated(true);
        roles = new ArrayList<>();
        roles.add(Role.USER);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser) {
        existingUser.setLastActivity(System.currentTimeMillis());
        return userRepository.save(existingUser);
    }

}