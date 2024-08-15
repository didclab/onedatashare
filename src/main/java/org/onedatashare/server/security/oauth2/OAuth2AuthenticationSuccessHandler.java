package org.onedatashare.server.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import org.onedatashare.server.model.util.CookieUtils;
import org.onedatashare.server.service.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.onedatashare.server.model.core.ODSConstants.*;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private OAuth2AuthorizationRequestRepositoryCookie oauth2AuthorizationRequestRepositoryCookie;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        List<String> redirectParams = getDetails(request, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + redirectParams.get(0));
            return;
        }
        CookieUtils.addCookie(response, TOKEN_COOKIE_NAME, redirectParams.get(1), Math.toIntExact(JWTUtil.getExpirationTime()), true);
        CookieUtils.addCookie(response, SAVE_OAUTH_TOKENS, String.valueOf(true), Math.toIntExact(JWTUtil.getExpirationTime()), false);
        CookieUtils.addCookie(response, USER_EMAIL, redirectParams.get(2), Math.toIntExact(JWTUtil.getExpirationTime()), false);
        CookieUtils.addCookie(response, COMPACT_VIEW_ENABLED, String.valueOf(false), Math.toIntExact(JWTUtil.getExpirationTime()), false);
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, redirectParams.get(0));
    }

    protected List<String> getDetails(HttpServletRequest request, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String token = jwtUtil.createToken(authentication);

        String email = jwtUtil.getEmailFromToken(token);

        List<String> result = new ArrayList<>();
        result.add(UriComponentsBuilder.fromUriString(targetUrl).build().toUriString());
        result.add(token);
        result.add(email);
        return result;
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

}