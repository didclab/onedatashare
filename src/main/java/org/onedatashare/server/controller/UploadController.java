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

import lombok.Data;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.response.FineUploaderResponse;
import org.onedatashare.server.model.useraction.IdMap;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.onedatashare.server.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;

@RestController
@RequestMapping("/api/stork")
public class UploadController {

    @Autowired
    UploadService uploadService;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Object> upload(@RequestPart("qqfile") Mono<FilePart> filePart,
                               @RequestPart("qquuid") String uploadUUID,
                               @RequestPart("qqfilename") String fileName,
                               @RequestPart(value = "qqpartindex", required = false) String chunkNumber,
                               @RequestPart("qqtotalparts") String totalChunks,
                               @RequestPart("qqtotalfilesize") String fileSize,
                               @RequestPart("directoryPath") String pathToWrite,
                               @RequestPart("credential") String credential,
                               @RequestPart("id") String destFolderId,
                               @RequestPart("map") String idMap,
                               Mono<Principal> principalMono){
        return principalMono.map(Principal::getName)
                .flatMap(userId -> uploadService.uploadChunk(userId, uploadUUID,
                        filePart, credential, pathToWrite, fileName, fileSize, idMap,
                        chunkNumber, totalChunks)
                        .map(x -> {
                            if(x) {
                                return FineUploaderResponse.ok();
                            }
                            return FineUploaderResponse.error();
                        })
                );
    }
}

