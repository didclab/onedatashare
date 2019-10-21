package org.onedatashare.server.service;

import com.jcraft.jsch.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
public class SSHConsoleService {

    JSch jsch;

    public SSHConsoleService(){
        jsch = new JSch();
    }

    public Mono<Session> createSession(String uri, String userName, String password, Integer port) {
        return Mono.create(sink -> {
            Session session = null;
            try {
                session = jsch.getSession(userName, uri, port);
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
                .flatMap(session -> {
                    try {
                        session.connect();
                        Channel channel = session.openChannel("exec");
                        ((ChannelExec) channel).setCommand(commandWithPath);
                        channel.setInputStream(null);
                        ((ChannelExec) channel).setErrStream(System.err);
                        InputStream in = channel.getInputStream();
                        channel.connect();

                        return readData(in, channel);
                    }
                    catch(Exception e){
                        ODSLoggerService.logError("Error occurred while executing SSH command");
                        e.printStackTrace();
                        return Flux.error(new Exception("Error occurred while executing SSH command"));
                    }
                });
    }

    public Flux<String> readData(InputStream in, Channel channel){
        try {
            String output = new String();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output = output + new String(tmp, 0, i);
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (Exception ee) {
                }
            }
            return Flux.just(output);
        }
        catch(Exception e){
            ODSLoggerService.logError("Error occurred while executing SSH command");
            e.printStackTrace();
            return Flux.error(new Exception("Error occurred while executing SSH command"));
        }

//        return Flux.generate(sink ->{
//            try {
//                byte[] tmp=new byte[1024];
//                while(true){
//                    while(in.available()>0){
//                        int i=in.read(tmp, 0, 1024);
//                        if(i<0)break;
//                        sink.next(new String(tmp,0, i));
//                    }
//                    if(channel.isClosed()){
//                        if(in.available()>0) continue;
//                        sink.complete();
//                        break;
//                    }
//                    try{Thread.sleep(500);}catch(Exception ee){}
//                }
//
//            }
//            catch(Exception e) {
//                ODSLoggerService.logError("Error occurred while reading result of SSH command execution");
//                sink.error(new Exception("Error occurred while reading result of SSH command execution"));
//            }
//        });
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