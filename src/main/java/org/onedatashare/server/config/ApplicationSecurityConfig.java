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

import org.onedatashare.server.service.ODSAuthenticationManager;
import org.onedatashare.server.service.ODSSecurityConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ApplicationSecurityConfig {

    @Autowired
    private ODSAuthenticationManager odsAuthenticationManager;

    @Autowired
    private ODSSecurityConfigRepository odsSecurityConfigRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic().disable()
                .authenticationManager(odsAuthenticationManager)
                .securityContextRepository(odsSecurityConfigRepository)
                .authorizeExchange()
                //Permit all the HTTP methods
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/api/stork/admin/**").hasAuthority("ADMIN")
                //Need authentication for APICalls
                .pathMatchers("/api/stork/ticket**").permitAll()
                .pathMatchers("/api/**").authenticated()
                //Need to be admin to access admin functionalities
                //TODO: Check if this setting is secure
                .pathMatchers("/**").permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(this::authenticationFailedHandler).accessDeniedHandler(this::accessDeniedHandler)
                .and()
                .csrf().disable().authorizeExchange().and()
                .build();

    }

    private Mono<Void> authenticationFailedHandler(ServerWebExchange serverWebExchange, AuthenticationException e) {
            return Mono.fromRunnable(() -> {
                serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            });
    }

    private Mono<Void> accessDeniedHandler(ServerWebExchange serverWebExchange, AccessDeniedException e) {
        return Mono.fromRunnable(() -> {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        });
    }
}
