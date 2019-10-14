package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.SSHCommandData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.SSHConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/stork/ssh/console")
public class SSHConsoleController {

    @Autowired
    SSHConsoleService consoleService;

    @PostMapping
    public Flux<String> runCommand(@RequestHeader  HttpHeaders headers, @RequestBody SSHCommandData commandData){

        UserAction ua = UserAction.convertToUserAction(commandData);
        return consoleService.runCommand(ua, commandData.getCommandWithPath());
    }


}
