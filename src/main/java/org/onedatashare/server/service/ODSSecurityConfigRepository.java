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


package org.onedatashare.server.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.onedatashare.server.model.core.ODSConstants.TOKEN_PREFIX;

@Service
public class ODSSecurityConfigRepository implements ServerSecurityContextRepository {

    @Autowired
    private ODSAuthenticationManager odsAuthenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        return null;
    }

    public String fetchAuthToken(ServerWebExchange serverWebExchange){
        ServerHttpRequest request = serverWebExchange.getRequest();

        String token = null;
        String endpoint = request.getPath().pathWithinApplication().value().toString();

        //Check for token only when the request needs to be authenticated
        if(endpoint.startsWith("/api/")) {
            try {
                // Try fetching token from the headers
                token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if(token != null && token.startsWith(TOKEN_PREFIX)){
                    token = token.substring(TOKEN_PREFIX.length());
                }
                // Try fetching token from the cookies
                else if(token == null) {
                  token = request.getCookies().getFirst("ATOKEN").getValue();
                }
            } catch (NullPointerException npe) {
                ODSLoggerService.logError("No token Found for request at " + endpoint);
            }
        }
        return token;
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        String authToken = this.fetchAuthToken(serverWebExchange);
        try {
            if (authToken != null) {
                String email = jwtUtil.getEmailFromToken(authToken);
                Authentication auth = new UsernamePasswordAuthenticationToken(email, authToken);
                return this.odsAuthenticationManager.authenticate(auth).map(SecurityContextImpl::new);
            }
        }
        catch(ExpiredJwtException e){
            ODSLoggerService.logError("Token Expired");
        }
        return Mono.empty();
    }
}
