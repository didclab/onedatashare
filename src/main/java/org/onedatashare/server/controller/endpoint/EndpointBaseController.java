package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.request.OperationRequestData;
import org.onedatashare.server.model.request.RequestData;
import org.onedatashare.server.model.util.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

public abstract class EndpointBaseController {

    @GetMapping("/oauth")
    public Rendering oauth(@RequestBody RequestData requestData){
        return oauthOperation();
    }

    @PostMapping("/ls")
    public Mono<Stat> list(@RequestBody RequestData requestData){
        return listOperation(requestData);
    }

    @PostMapping("/mkdir")
    public Mono<Response> mkdir(@RequestBody OperationRequestData operationRequestData){
        return mkdirOperation(operationRequestData);
    }

    @PostMapping("/rm")
    public Mono<Response> delete(@RequestBody OperationRequestData operationRequestData){
        return deleteOperation(operationRequestData);
    }

    @PostMapping("/upload")
    public Mono<Stat> upload(){
        return uploadOperation();
    }

    @PostMapping("/download")
    public Mono download(@RequestBody RequestData requestData){
        return downloadOperation(requestData);
    }

    protected abstract Mono<Stat> listOperation(RequestData requestData);
    protected abstract Mono<Response> mkdirOperation(OperationRequestData operationRequestData);
    protected abstract Mono<Response> deleteOperation(OperationRequestData operationRequestData);
    protected abstract Mono<Stat> uploadOperation();
    protected abstract Mono<String> downloadOperation(RequestData requestData);
    protected abstract Rendering oauthOperation();
}