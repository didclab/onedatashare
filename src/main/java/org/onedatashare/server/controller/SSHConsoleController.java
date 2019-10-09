package org.onedatashare.server.controller;

import org.onedatashare.server.model.requestdata.SSHCommandData;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.service.SSHConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/stork/ssh/console")
public class SSHConsoleController {

    @Autowired
    SSHConsoleService consoleService;

    @PostMapping
    public Object runCommand(@RequestHeader  HttpHeaders headers, @RequestBody SSHCommandData commandData){

        UserAction ua = UserAction.convertToUserAction(commandData);
        consoleService.runCommand(ua);

    }


}
