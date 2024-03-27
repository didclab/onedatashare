package org.onedatashare.server.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.model.util.CookieUtils;
import org.onedatashare.server.repository.UserRepository;
import org.onedatashare.server.service.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.onedatashare.server.model.core.ODSConstants.TOKEN_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    private List<Role> roles;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private OAuth2AuthorizationRequestRepositoryCookie oauth2AuthorizationRequestRepositoryCookie;
    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //TODO: Need to discuss about this flow when compared to Generic OAuth
//        if(authentication.getPrincipal() instanceof OidcUser) {
//            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
//            String email = oidcUser.getEmail();
//            if(StringUtils.isEmpty(email)) {
//                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
//            }
//            Optional<User> userOptional = userRepository.findById(email);
//            if (userOptional.isEmpty()) {
//                registerNewUserOidc(oidcUser);
//            }
//        }
//        else if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
//            DefaultOAuth2User userDetails = (DefaultOAuth2User) authentication.getPrincipal();
//            String email = userDetails.getAttribute("email") != null ? userDetails.getAttribute("email") : userDetails.getAttribute("login") + "@gmail.com";
//            if(StringUtils.isEmpty(email)) {
//                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
//            }
//            Optional<User> userOptional = userRepository.findById(email);
//            if (userOptional.isEmpty()) {
//                registerNewUserOauth(userDetails);
//            }
//        }

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String token = jwtUtil.generateToken(authentication);

        String email = jwtUtil.getEmailFromToken(token);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam(TOKEN_COOKIE_NAME, token)
                .queryParam("email", email)
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        oauth2AuthorizationRequestRepositoryCookie.removeAuthorizationRequestCookies(request, response);
    }

    //TODO: Need to discuss about this flow when compared to Generic OAuth
//    private User registerNewUserOidc(OidcUser oidcUser) {
//        User user = new User();
//        user.setFirstName(oidcUser.getGivenName());
//        user.setLastName(oidcUser.getFamilyName());
//        user.setEmail(oidcUser.getEmail());
//        roles.add(Role.USER);
//        user.setRoles(roles);
//        return userRepository.save(user);
//    }
//    private User registerNewUserOauth(DefaultOAuth2User oAuth2User) {
//        User user = new User();
//        user.setFirstName(oAuth2User.getAttribute("name"));
//        user.setEmail(oAuth2User.getAttribute("email"));
//        roles.add(Role.USER);
//        user.setRoles(roles);
//        return userRepository.save(user);
//    }

}