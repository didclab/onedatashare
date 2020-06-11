package org.onedatashare.server.model;

import com.jcraft.jsch.UserInfo;

public class SSHUserInfo implements UserInfo {
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
