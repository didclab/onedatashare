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

import io.jsonwebtoken.Claims;
import org.onedatashare.server.model.core.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ODSAuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        String userName = null;
        try{
            userName = jwtUtil.getEmailFromToken(authToken);
        } catch (Exception e){
            Mono.empty();
        }

        if(userName != null && jwtUtil.validateToken(authToken)){
            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            List<String> roleList = claims.get("role", List.class);
            List<Role> roles = new ArrayList<>();
            if(roleList != null) {
                for (String role : roleList) {
                    roles.add(Role.valueOf(role));
                }
            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userName,
                    authToken,
                    roles.stream().map(authority -> new SimpleGrantedAuthority(authority.name())).collect(Collectors.toList())
            );
            return Mono.just(auth);
        }
        return Mono.empty();
    }
}
