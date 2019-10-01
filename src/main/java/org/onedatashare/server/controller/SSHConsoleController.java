package org.onedatashare.server.controller;

import org.onedatashare.server.service.SSHConsoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/api/stork/ssh/console")
public class SSHConsoleController {

    @Autowired
    SSHConsoleService consoleService;
//
//    @PostMapping
//    public Object runCommand(@RequestHeader  HttpHeaders headers){
//
//    }


}
