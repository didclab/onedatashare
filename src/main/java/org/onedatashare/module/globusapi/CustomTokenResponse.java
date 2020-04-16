//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.onedatashare.module.globusapi;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;
import sun.util.calendar.BaseCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Date;

public class CustomTokenResponse extends GenericJson {
    @Key("access_token")
    private String accessToken;
    @Key("token_type")
    private String tokenType;
    @Key("expires_in")
    private Integer expiresInSeconds;

    @Key("resource_server")
    public String resource_server;
    @Key("refresh_token")
    private String refreshToken;
    @Key
    private String scope;
    @Key("other_tokens")
    private ArrayList<LinkedHashMap> other_token;

    private Calendar recievedTime;

    public CustomTokenResponse() {
        recievedTime = Calendar.getInstance();
        recievedTime.add(Calendar.SECOND, 172800);
    }

    public final String getTransferAccessToken() {
        for(int i = 0; i < other_token.size(); i++){
            if("transfer.api.globus.org".equals(other_token.get(i).get("resource_server"))){
                return (String)other_token.get(i).get("access_token");
            }
        }
        return "not found";
    }

    public final Date getExpiredTime() {
        for(int i = 0; i < other_token.size(); i++){
            if("transfer.api.globus.org".equals(other_token.get(i).get("resource_server"))){
                return recievedTime.getTime();
            }
        }
        return null;
    }

    public final String getResourceServer(){
        return this.resource_server;
    }

    public CustomTokenResponse setAccessToken(String accessToken) {
        this.accessToken = (String)Preconditions.checkNotNull(accessToken);
        return this;
    }

    public final String getTokenType() {
        return this.tokenType;
    }

    public CustomTokenResponse setTokenType(String tokenType) {
        this.tokenType = (String)Preconditions.checkNotNull(tokenType);
        return this;
    }

    public final Integer getExpiresInSeconds() {
        return this.expiresInSeconds;
    }

    public CustomTokenResponse setExpiresInSeconds(Integer expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
        return this;
    }

    public final String getRefreshToken() {
        return this.refreshToken;
    }

    public CustomTokenResponse setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public final String getScope() {
        return this.scope;
    }

    public CustomTokenResponse setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public CustomTokenResponse set(String fieldName, Object value) {
        return (CustomTokenResponse)super.set(fieldName, value);
    }

    public CustomTokenResponse clone() {
        return (CustomTokenResponse)super.clone();
    }
}
