package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.UnsupportedOperationException;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.GdriveService;
import org.onedatashare.server.service.oauth.GoogleDriveOauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/gdrive")
public class GdriveController extends EndpointBaseController{
    @Autowired
    private GdriveService gdriveService;

    @Autowired
    private GoogleDriveOauthService googleDriveOauthService;

    @Override
    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return gdriveService.list(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return gdriveService.mkdir(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return gdriveService.delete(null, userAction).map(this::returnOnSuccess).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Stat> uploadOperation() {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    protected Mono<String> downloadOperation(RequestData requestData){
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return gdriveService.download(null, userAction);
    }

    @Override
    protected Rendering oauthOperation() {
        return Rendering.redirectTo(googleDriveOauthService.start()).build();
    }
}
