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

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    @Autowired
    private ODSAuthenticationManager odsAuthenticationManager;

    @Autowired
    private ODSSecurityConfigRepository odsSecurityConfigRepository;

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

}
