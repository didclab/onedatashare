package org.onedatashare.server.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.UserInfo;
import org.onedatashare.server.model.useraction.UserAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SSHConsoleService {

    @Autowired
    JSch jsch;

    public Mono<String> runCommand(UserAction ua){
        return Mono.just(new SSHUserInfo(ua.getCredential().getPassword()))
                .map(userInfo -> jsch.getSession(ua.getUri(), ua.getCredential().getUsername(), ua.getPortNumber()));
    }
}


class SSHUserInfo implements UserInfo{

    private String password;

    public SSHUserInfo(String pwd){
        this.password = pwd;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String s) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String s) {
        return false;
    }

    @Override
    public boolean promptYesNo(String s) {
        return true;
    }

    @Override
    public void showMessage(String s) {
    }
}