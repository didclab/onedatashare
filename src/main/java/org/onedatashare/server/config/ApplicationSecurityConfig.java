/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.onedatashare.server.model.core.AuthProvider;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.security.oauth2.*;
import org.onedatashare.server.service.ODSAuthenticationManager;
import org.onedatashare.server.service.ODSSecurityConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    @Autowired
    private ODSAuthenticationManager odsAuthenticationManager;

    @Autowired
    private ODSSecurityConfigRepository odsSecurityConfigRepository;

    @Autowired
    private OAuth2UserService OAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private OAuth2AuthorizationRequestRepositoryCookie OAuth2AuthorizationRequestRepositoryCookie;

    @Bean
    public OAuth2AuthorizationRequestRepositoryCookie cookieAuthorizationRequestRepository() {
        return new OAuth2AuthorizationRequestRepositoryCookie();
    }

    private final AppPropertiesForCredentials appProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .authenticationManager(odsAuthenticationManager)
                .securityContext((httpSecuritySecurityContextConfigurer ->
                        httpSecuritySecurityContextConfigurer.securityContextRepository(odsSecurityConfigRepository)))
                .authorizeHttpRequests(requests -> {
                    //Permit all the HTTP methods
                        requests.requestMatchers(HttpMethod.OPTIONS).permitAll()
                                .requestMatchers("/api/stork/admin/**").hasAuthority("ADMIN")
                                //Need authentication for APICalls
                                .requestMatchers("/api/stork/ticket**").permitAll()
                                .requestMatchers("/api/**").authenticated()
                                //Need to be admin to access admin functionalities
                                //TODO: Check if this setting is secure
                                .requestMatchers("/**").permitAll();
                })
                .oauth2Login()
                .authorizationEndpoint()
                        .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .userInfoEndpoint()
                    .userService(OAuth2UserService)
                .and()
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                .and()
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.authenticationEntryPoint(this::authenticationFailedHandler)
                                .accessDeniedHandler(this::accessDeniedHandler))
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }
    private void accessDeniedHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) {
        httpServletResponse.setStatus(HttpStatus.FORBIDDEN.value());
    }

    private void authenticationFailedHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) {
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowBackSlash(true);
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowUrlEncodedSlash(true);
        return (web) -> web.httpFirewall(firewall);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(getCilogonClientRegistration(), getGithubClientRegistration(), getGoogleClientRegistration());
    }

    private ClientRegistration getGithubClientRegistration() {
        AppPropertiesForCredentials.OAuth2ClientProperties clientPropertiesOfGitHub = appProperties.getClients().get(String.valueOf(AuthProvider.github));
        return CommonOAuth2Provider.GITHUB.getBuilder(String.valueOf(AuthProvider.github)).clientId(clientPropertiesOfGitHub.getClientId())
                .clientSecret(clientPropertiesOfGitHub.getClientSecret()).build();
    }
    private ClientRegistration getGoogleClientRegistration() {
        AppPropertiesForCredentials.OAuth2ClientProperties clientPropertiesOfGoogle = appProperties.getClients().get(String.valueOf(AuthProvider.google));
        return CommonOAuth2Provider.GOOGLE.getBuilder(String.valueOf(AuthProvider.google)).clientId(clientPropertiesOfGoogle.getClientId())
                .clientSecret(clientPropertiesOfGoogle.getClientSecret()).build();
    }
    private ClientRegistration getCilogonClientRegistration() {
        AppPropertiesForCredentials.OAuth2ClientProperties clientPropertiesOfCilogon = appProperties.getClients().get(String.valueOf(AuthProvider.cilogon));
        return ClientRegistration.withRegistrationId(String.valueOf(AuthProvider.cilogon))
                .clientId(clientPropertiesOfCilogon.getClientId())
                .clientSecret(clientPropertiesOfCilogon.getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .scope("openid", "email", "profile")
                .authorizationUri(clientPropertiesOfCilogon.getAuthorizationUri())
                .tokenUri(clientPropertiesOfCilogon.getTokenUri())
                .userInfoUri(clientPropertiesOfCilogon.getUserinfoUri())
                .clientName(ODSConstants.CILOGON)
                .jwkSetUri(clientPropertiesOfCilogon.getJwtSetUri())
                .userNameAttributeName("sub")
                .build();
    }
}
