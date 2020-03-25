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


package org.onedatashare.server.controller;

import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.NotFoundException;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/stork/globus")
public class GlobusEndpointController {
    Mono<GlobusClient> gc;
    @Autowired
    private UserService userService;
    @PostMapping
    public Object globusRequest(@RequestHeader HttpHeaders headers, @RequestBody UserAction gea) {
        switch(gea.getAction()) {
            case "endpoint_list":
                return userService.getGlobusClient(headers.getFirst(ODSConstants.COOKIE)).flatMap(gc ->
                        gc.getEndPointList("all", "0", "100", gea.getFilter_fulltext()));
            case "endpoint":
                return userService.getGlobusClient(headers.getFirst(ODSConstants.COOKIE)).flatMap(gc ->
                        gc.getEndPoint(gea.getGlobusEndpoint().getId()));
            case "endpointId":
                if(gea.getGlobusEndpoint().getId() == null){
                    return userService.getEndpointId(headers.getFirst(ODSConstants.COOKIE));
                }
                return userService.saveEndpointId(UUID.fromString(gea.getGlobusEndpoint().getId()),
                                                  gea.getGlobusEndpoint(), headers.getFirst(ODSConstants.COOKIE)
                );
            case "deleteEndpointId":
                return userService.deleteEndpointId(headers.getFirst(ODSConstants.COOKIE), UUID.fromString(gea.getGlobusEndpoint().getId()));
            case "endpointActivate":
                return userService.getGlobusClient(headers.getFirst(ODSConstants.COOKIE)).flatMap(gc ->
                    gc.activateEndPoint(gea.getGlobusEndpoint().getId(), gea.getGlobusEndpoint().getMyProxyServer(),
                                        gea.getGlobusEndpoint().getMyProxyDomainName(), gea.getUsername(), gea.getPassword()))
                    .switchIfEmpty(Mono.error(new Exception("Auth Failed")));
            default:
                return Mono.error(new NotFoundException());
        }
    }
}
