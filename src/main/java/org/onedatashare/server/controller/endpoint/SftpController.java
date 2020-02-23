package org.onedatashare.server.controller.endpoint;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.UnsupportedOperationException;
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
import org.springframework.web.reactive.result.view.Rendering;
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

    @Override
    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return vfsService.list(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.mkdir(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.delete(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Stat> uploadOperation() {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    protected Mono<String> downloadOperation(@RequestBody RequestData requestData){
        return Mono.error(new UnsupportedOperationException());
    }
    @Override
    protected Rendering oauthOperation() {
        throw new UnsupportedOperationException();
    }

    @PostMapping(value = "/file")
    public Mono<ResponseEntity> downloadFile(@RequestHeader HttpHeaders clientHttpHeaders) throws IOException {
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