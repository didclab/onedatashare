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


package org.onedatashare.server.controller.endpoint;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.UnsupportedOperationException;
import org.onedatashare.server.model.filesystem.operations.ListOperation;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Set;

@RestController
@RequestMapping("/api/sftp")
public class SftpController extends EndpointBaseController{
    @Autowired
    private VfsService vfsService;

    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
//        return vfsService.list(null, userAction).subscribeOn(Schedulers.elastic());
        return null;
    }

    @Override
    protected Mono<Stat> listOperation(ListOperation listOperation) {
        return null;
    }

    @Override
    protected Mono<Void> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.mkdir(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Void> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.delete(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<String> downloadOperation(@RequestBody RequestData requestData){
        return Mono.error(new UnsupportedOperationException());
    }

    @PostMapping(value = "/file")
    public Mono<ResponseEntity> downloadFile(@RequestHeader HttpHeaders clientHttpHeaders) {
        return Mono.fromSupplier(() -> {
            String cookie = clientHttpHeaders.getFirst(ODSConstants.COOKIE);
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookie);
            String cx = null;
            for (Cookie c : cookies) {
                if (c.name().equals("CX")) {
                    cx = c.value();
                    break;
                }
            }
            if(cx == null) {
                ODSLoggerService.logError("Cookie not found");
                throw new RuntimeException("Missing Cookie");
            }

            UserActionResource userActionResource = null;
            try {
                // Replacing all the occurrence of '+' characters with its URL encoded equivalent '%2b'
                // since URLDecoder decodes '+' character as a space as per URL encoding standards
                cx = cx.replaceAll("\\+", "%2b");
                final String userActionResourceString = URLDecoder.decode(cx, "UTF-8");
                ObjectMapper objectMapper = new ObjectMapper();
                userActionResource = objectMapper.readValue(userActionResourceString, UserActionResource.class);
            } catch (IOException e) {
                Mono.error(e);
            }
            return userActionResource;
        }).flatMap(userActionResource -> vfsService.getSftpDownloadStream(null, userActionResource))
                .subscribeOn(Schedulers.elastic());
    }

}