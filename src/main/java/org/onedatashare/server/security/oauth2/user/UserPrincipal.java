package org.onedatashare.server.security.oauth2.user;

import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.core.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

public class UserPrincipal implements OAuth2User, UserDetails, OidcUser {
    private String email;
    private String firstName;
    private String lastName;
    private String organisation;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private OidcIdToken idToken;

    public UserPrincipal(String email, String firstName, String lastName, String organisation, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.authorities = authorities;
        this.organisation = organisation;
    }

    public static UserPrincipal create(User user) {
        // Have firstName, lastName and organisation here
        List<Role> roles = user.getRoles();
        return new UserPrincipal(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getOrganization(),
                roles.stream().map(authority -> new SimpleGrantedAuthority(authority.name())).collect(Collectors.toList())
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes, OidcIdToken idToken) {
        UserPrincipal userPrincipal = UserPrincipal.create(user, attributes);
        userPrincipal.setIdToken(idToken);
        return userPrincipal;
    }


    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganisation() {
        return organisation;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Collections.emptyMap();
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }
    @Override
    public OidcUserInfo getUserInfo() {
        return new OidcUserInfo(getClaims());
    }
    public void setIdToken(OidcIdToken idToken) {
        this.idToken = idToken;
    }
    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}
