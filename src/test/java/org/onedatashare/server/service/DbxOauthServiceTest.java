package org.onedatashare.server.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.onedatashare.server.service.oauth.DbxOauthService;
import org.springframework.beans.factory.annotation.Autowired;

public class DbxOauthServiceTest {

    @Autowired
    private DbxOauthService dbxOauthService;

    @Test
    @DisplayName("API access key and secret key")
    @Disabled
    public void apiKeys(){
        assertTrue(dbxOauthService.keysNotNull(), "Missing access/secret key");
    }

    @Test
    public void start_givenNothing_throwsRuntimeException() {
    }
}