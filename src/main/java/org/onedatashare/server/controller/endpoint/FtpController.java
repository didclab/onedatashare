package org.onedatashare.server.controller.endpoint;

import org.apache.commons.lang.NotImplementedException;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.util.Response;
import org.onedatashare.server.service.VfsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/ftp")
public class FtpController extends EndpointBaseController{
    @Autowired
    private VfsService vfsService;

    @Override
    protected Mono<Stat> listOperation(RequestData requestData) {
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return vfsService.list(null, userAction).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Response> mkdirOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.mkdir(null, userAction).map(res -> new Response("Success", 200)).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Response> deleteOperation(OperationRequestData operationRequestData) {
        UserAction userAction = UserAction.convertToUserAction(operationRequestData);
        return vfsService.delete(null, userAction).map(res -> new Response("Success", 200)).subscribeOn(Schedulers.elastic());
    }

    @Override
    protected Mono<Stat> uploadOperation() {
        return Mono.error(new NotImplementedException("UnSupport Operation"));
    }

    @Override
    protected Mono<String> downloadOperation(RequestData requestData){
        UserAction userAction = UserAction.convertToUserAction(requestData);
        return vfsService.download(null, userAction);
    }

    @Override
    protected Rendering oauthOperation() {
        return null;
    }
}