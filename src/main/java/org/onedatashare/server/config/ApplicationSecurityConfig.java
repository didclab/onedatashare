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
                .pathMatchers("/api/stork/**").authenticated()
                .pathMatchers("/api/**").authenticated()
                //Need to be admin to access admin functionalities
                //TODO: Check if this setting is secure
                .pathMatchers("/**").permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(this::authenticationEntryPointHandler).accessDeniedHandler(this::accessDeniedHandler)
                .and()
                //Disable Cross-site request forgery TODO: fix
                .csrf().disable().authorizeExchange().and()
                .build();

    }

    private Mono<Void> authenticationEntryPointHandler(ServerWebExchange serverWebExchange, AuthenticationException e) {
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