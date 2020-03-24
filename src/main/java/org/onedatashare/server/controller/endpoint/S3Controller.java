package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequestMapping("/api/amazons3")
@RestController
public class S3Controller extends EndpointBaseController{
    @Autowired
    private S3Service s3Service;

    @Override
    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return s3Service.list(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<ResponseEntity> mkdirOperation(OperationRequestData operationRequestData) {
        return null;
    }

    @Override
    protected Mono<ResponseEntity> deleteOperation(OperationRequestData operationRequestData) {
        return null;
    }

    @Override
    protected Mono<Stat> uploadOperation() {
        return null;
    }

    @Override
    protected Mono<String> downloadOperation(RequestData requestData) {
        return null;
    }

    @Override
    protected Rendering oauthOperation() {
        return null;
    }
}
