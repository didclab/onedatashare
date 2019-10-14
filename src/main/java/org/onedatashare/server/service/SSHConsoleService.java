package org.onedatashare.server.service;

import com.jcraft.jsch.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Service
public class SSHConsoleService {

    @Autowired
    JSch jsch;

    public Mono<Session> createSession(String uri, String userName, String password, Integer port) {
        return Mono.create(sink -> {
            Session session = null;
            try {
                session = jsch.getSession(uri, userName, port);
            } catch (Exception e) {
                ODSLoggerService.logError("Error occurred while trying to create SSH session for " + userName);
                sink.error(new Exception("Failed to create SSH session"));
            }
            session.setUserInfo(new SSHUserInfo(password));
            sink.success(session);
        });
    }    //createSession()

    public Flux<Object> runCommand(UserAction ua, String commandWithPath){
        return createSession(ua.getUri(), ua.getCredential().getUsername(), ua.getCredential().getPassword(), Integer.parseInt(ua.getPortNumber()))
                .flux()
                .map(session -> {
                    try {
                        session.connect();
                        Channel channel = session.openChannel("exec");
                        ((ChannelExec) channel).setCommand(commandWithPath);
                        channel.setInputStream(null);
                        ((ChannelExec) channel).setErrStream(System.err);
                        InputStream in = channel.getInputStream();
                        channel.connect();

                        return readData(in);
                    }
                    catch(Exception e){
                        ODSLoggerService.logError("Error occurred while executing SSH command");
                        e.printStackTrace();
                        return Flux.error(new Exception("Error!"));
                    }
                });
    }

    public Flux<String> readData(InputStream in){
        return Flux.generate(sink ->{
                    try {
                        while (in.available() > 0) {
                            byte[] tmp = new byte[1024];
                            int bytes = in.read(tmp, 0, 1024);
                            sink.next(new String(tmp, 0, bytes));
                        }
                    }
                    catch(Exception e){
                        ODSLoggerService.logError("Error occurred while reading result of SSH command execution");
                        sink.error(new Exception("Error occurred while reading result of SSH command execution"));
                    }
                    sink.complete();
                });
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