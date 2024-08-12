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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

import static org.onedatashare.server.model.core.ODSConstants.TOKEN_PREFIX;

@Service
public class ODSSecurityConfigRepository implements SecurityContextRepository {

    @Autowired
    private ODSAuthenticationManager odsAuthenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    public String fetchAuthToken(HttpRequestResponseHolder requestResponseHolder){
        HttpServletRequest request = requestResponseHolder.getRequest();

        String token = null;
        String endpoint = request.getContextPath()+request.getServletPath();

        //Check for token only when the request needs to be authenticated
        if(endpoint.startsWith("/api/")) {
            try {
                // Try fetching token from the headers
                token = request.getHeader(HttpHeaders.AUTHORIZATION);
                if(token != null && token.startsWith(TOKEN_PREFIX)){
                    token = token.substring(TOKEN_PREFIX.length());
                }
                // Try fetching token from the cookies
                else if(token == null) {
                  Optional<Cookie> cookieValue = Arrays.stream(request.getCookies()).filter(cookie->"ATOKEN".equals(cookie.getName())).findFirst();
                  if(!cookieValue.isPresent()){
                      throw new NullPointerException();
                  }
                  token=cookieValue.get().getValue();
                }
            } catch (NullPointerException npe) {
                ODSLoggerService.logError("No token Found for request at " + endpoint);
            }
        }
        return token;
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        String authToken = this.fetchAuthToken(requestResponseHolder);
        try {
            if (authToken != null) {
                String email = jwtUtil.getEmailFromToken(authToken);
                Authentication auth = new UsernamePasswordAuthenticationToken(email, authToken);
                return new SecurityContextImpl(this.odsAuthenticationManager.authenticate(auth));
            }
        }
        catch(ExpiredJwtException e){
            ODSLoggerService.logError("Token Expired");
            throw e;
        }
        return null;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }
}
