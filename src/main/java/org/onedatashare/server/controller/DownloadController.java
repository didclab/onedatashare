package org.onedatashare.server.controller;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.error.AuthenticationRequired;
import org.onedatashare.server.model.error.TokenExpiredException;
import org.onedatashare.server.model.requestdata.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.service.DbxService;
import org.onedatashare.server.service.ODSLoggerService;
import org.onedatashare.server.service.ResourceServiceImpl;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller that request to cancel a transfer that is in progress.
 */
@RestController
@RequestMapping("/api/stork/download")
public class DownloadController {

    @Autowired
    private DbxService dbxService;

    @Autowired
    private VfsService vfsService;

    @Autowired
    private ResourceServiceImpl resourceService;

    /**
     * Handler that returns the download link for the requested file in requestData
     * @param headers - Incoming request headers
     * @param requestData - Request data needed to generate the download link
     * @return - Mono\<String\> containing the download link
     */
    @PostMapping
    public Object download(@RequestHeader HttpHeaders headers, @RequestBody RequestData requestData) {

        String cookie = headers.getFirst(ODSConstants.COOKIE);
        UserAction userAction = UserAction.convertToUserAction(requestData);
        if (userAction.getUri().startsWith(ODSConstants.DROPBOX_URI_SCHEME)) {
            return dbxService.getDownloadURL(cookie, userAction);
        } else if (ODSConstants.DRIVE_URI_SCHEME.equals(userAction.getType())) {
            if (userAction.getCredential() == null) {
                return new ResponseEntity<>(new AuthenticationRequired("oauth"), HttpStatus.INTERNAL_SERVER_ERROR);
            } else return resourceService.download(cookie, userAction);
        } else if (userAction.getUri().startsWith(ODSConstants.FTP_URI_SCHEME)) {
            return vfsService.getDownloadURL(cookie, userAction);
        }
        return null;
    }

    @RequestMapping(value = "/file", method = RequestMethod.GET)
    public Mono<ResponseEntity> getAcquisition(@RequestHeader HttpHeaders clientHttpHeaders) throws IOException{
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

        // Replacing all the occurrence of '+' characters with its URL encoded equivalent '%2b'
        // since URLDecoder decodes '+' character as a space as per URL encoding standards
        cx = cx.replaceAll("\\+", "%2b");
        final String userActionResourceString = URLDecoder.decode(cx, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        UserActionResource userActionResource = objectMapper.readValue(userActionResourceString, UserActionResource.class);
        return vfsService.getSftpDownloadStream(cookie, userActionResource);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<String> handle(TokenExpiredException tokenExpiredException) {
        return new ResponseEntity<>(tokenExpiredException.toString(), tokenExpiredException.status);
    }

}
