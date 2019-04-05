package org.onedatashare.server.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onedatashare.server.service.oauth.DbxOauthService;
import org.springframework.beans.factory.annotation.Autowired;

public class DbxOauthServiceTest {

    @Autowired
    private DbxOauthService dbxOauthService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void start_givenNothing_throwsRuntimeException() {
        thrown.expect(RuntimeException.class);
        dbxOauthService.start();
    }
}