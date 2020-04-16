package org.onedatashare.server.controller.endpoint;

import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.error.UnsupportedOperationException;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.GridFtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
@RequestMapping("/api/gsiftp/")
public class GridFtpController{// extends OAuthEndpointBaseController{
    @Autowired
    private GridFtpService gridftpService;

    protected Rendering redirectTo(String url){
        return Rendering.redirectTo(url).build();
    }

    protected Mono<Stat> listOperation(UserAction userAction) {
        return gridftpService.list(null, userAction);
    }

    protected Mono<Boolean> mkdirOperation(UserAction userAction) {
        return gridftpService.mkdir(null, userAction);
    }

    protected Mono<Boolean> deleteOperation(UserAction userAction) {
        return gridftpService.delete(null, userAction);
    }

    protected Mono<Stat> uploadOperation() {
        return Mono.error(new UnsupportedOperationException());
    }

    protected Mono<String> downloadOperation(UserAction userAction){
        return Mono.error(new UnsupportedOperationException());
    }

//    @Override
    protected Mono<Rendering> initiateOauthOperation() {
        return gridftpService.getOAuthUrl()
                .map(this::redirectTo);
    }

//    @Override
    protected Mono<Rendering> completeOauthOperation(Map<String, String> queryParameters) {
        return gridftpService.completeOAuth(queryParameters).map(this::redirectTo);
    }

    @GetMapping(value = "/initiate-oauth")
    public Mono<Rendering> initiateOauth(){
        return initiateOauthOperation();
    }

    @GetMapping(value = "/complete-oauth")
    public Mono<Rendering> completeOauth(@RequestParam Map<String, String> queryParameters){
        return completeOauthOperation(queryParameters);
    }

    @PostMapping("/ls")
    public @ResponseBody Mono<Stat> list(@RequestBody UserAction userAction){
        return listOperation(userAction);
    }

    @PostMapping("/mkdir")
    public @ResponseBody
    Mono<Boolean> mkdir(@RequestBody UserAction userAction){
        return mkdirOperation(userAction);
    }

    @PostMapping("/rm")
    public @ResponseBody
    Mono<Boolean> delete(@RequestBody UserAction userAction){
        return deleteOperation(userAction);
    }

    @PostMapping("/upload")
    public @ResponseBody Mono<Stat> upload(){
        return uploadOperation();
    }

    @PostMapping("/download")
    public @ResponseBody Mono download(@RequestBody UserAction userAction){
        return downloadOperation(userAction);
    }

}