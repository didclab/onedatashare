package org.onedatashare.server.controller;

import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.requestdata.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling list requests on endpoints
 */
@RestController
@RequestMapping("/api/stork/ls")
public class ListController {

    @Autowired
    private DbxService dbxService;

    @Autowired
    private VfsService vfsService;

    @Autowired
    private GridftpService gridService;

    @Autowired
    private ResourceServiceImpl resourceService;

    @Autowired
    private BoxService boxService;

    @Autowired
    private HttpFileService httpService;

    /**
     * Handler that returns Mono of the stats(file information) in the given path of the endpoint
     * @param headers - Incoming request headers
     * @param requestData - Request data needed to generate the download link
     * @return Mono\<Stat\> containing the file/ folder information
     */
    @PostMapping
    public Object list(@RequestHeader HttpHeaders headers, @RequestBody RequestData requestData) {
        String cookie = headers.getFirst("cookie");
        UserAction userAction = UserAction.convertToUserAction(requestData);

        if(userAction.getCredential() == null) {
            switch (userAction.getType()) {
                case ODSConstants.DROPBOX_URI_SCHEME:
                case ODSConstants.DRIVE_URI_SCHEME:
                case ODSConstants.GRIDFTP_URI_SCHEME:
                case ODSConstants.BOX_URI_SCHEME:
                    return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        switch (userAction.getType()) {
            case ODSConstants.DROPBOX_URI_SCHEME:
                return dbxService.list(cookie, userAction);
            case ODSConstants.DRIVE_URI_SCHEME:
                return resourceService.list(cookie, userAction);
            case ODSConstants.GRIDFTP_URI_SCHEME:
                return gridService.list(cookie, userAction);
            case ODSConstants.BOX_URI_SCHEME:
                return boxService.list(cookie, userAction);
            case ODSConstants.SCP_URI_SCHEME:
            case ODSConstants.SFTP_URI_SCHEME:
            case ODSConstants.FTP_URI_SCHEME:
                return vfsService.list(cookie, userAction);
            case ODSConstants.HTTP_URI_SCHEME:
                return httpService.list(cookie, userAction);
            default:
                return null;
        }
    }

    @ExceptionHandler(AuthenticationRequired.class)
    public ResponseEntity<String> handle(AuthenticationRequired authenticationRequired) {
        return new ResponseEntity<>(authenticationRequired.toString(), authenticationRequired.status);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handle(TokenExpiredException tokenExpiredException) {
        return new ResponseEntity<>(tokenExpiredException.toString(), tokenExpiredException.status);
    }
}

