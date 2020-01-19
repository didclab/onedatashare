package org.onedatashare.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
////@EnableReactiveMethodSecurity
public class ApplicationSecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic().disable().authorizeExchange()
                //Permit all the HTTP methods
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                //Permit loginPoint
                .pathMatchers("/api/stork/**").authenticated()
                //TODO: Check if this setting is secure
                .pathMatchers("/**").permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    });
                }).accessDeniedHandler((swe, e) -> {
                    return Mono.fromRunnable(() -> {
                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    });
                }).and()
                //Disable Cross-site request forgery TODO: fix
                .csrf().disable().authorizeExchange().and()
                .formLogin().and()
                .build();

//                .csrf().disable()
//                .formLogin().disable()
//                .httpBasic().disable()
////                .authenticationManager(authenticationManager)
////                .securityContextRepository(securityContextRepository)
//                .authorizeExchange()
//                .pathMatchers(HttpMethod.OPTIONS).permitAll()
//                .pathMatchers("/loginpoint").permitAll()
//                .pathMatchers("/getToken").permitAll()
//                .anyExchange().authenticated()
//                .and().build();
    }

}
