package org.onedatashare.server.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/stork")
public class UploadController {

    @Autowired
    UploadService uploadService;

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Object> upload(@RequestHeader HttpHeaders headers,
                               @RequestPart("directoryPath") String directoryPath,
                               @RequestPart("qqfilename") String fileName,
                               @RequestPart("map") String idMap,
                               @RequestPart("credential") String credential,
                               @RequestPart("id") String googledriveid,
                               @RequestPart("qquuid") String fileUUID,
                               @RequestPart("qqtotalfilesize") String totalFileSize,
                               @RequestPart("qqfile") Mono<FilePart> filePart){

        String cookie = headers.getFirst(ODSConstants.COOKIE);
        if(directoryPath.startsWith(ODSConstants.SCP_URI_SCHEME)){
            directoryPath = directoryPath.replace(ODSConstants.SCP_URI_SCHEME, ODSConstants.SFTP_URI_SCHEME);
            idMap = idMap.replace(ODSConstants.SCP_URI_SCHEME, ODSConstants.SFTP_URI_SCHEME);
        }
        return uploadService.uploadChunk(cookie, UUID.fromString(fileUUID),
            filePart, credential, directoryPath, fileName,
            Long.parseLong(totalFileSize), googledriveid, idMap).map(job -> {
                FineUploaderResponse resp = new FineUploaderResponse();
                resp.success = true;
                return resp;
            });
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private class FineUploaderResponse {
        public boolean success;
        public boolean error;
    }
}

