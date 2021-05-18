package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.SSHCommandData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.SSHConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(value = "/api/ssh/console")
public class SSHConsoleController {

//    @Autowired
//    private SSHConsoleService consoleService;
//
//    @PostMapping
//    public Flux runCommand(@RequestBody SSHCommandData commandData){
//
//        UserAction ua = UserAction.convertToUserAction(commandData);
//        return consoleService.runCommand(ua, commandData.getCommandWithPath());
//    }
}
