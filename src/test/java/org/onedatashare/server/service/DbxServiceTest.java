/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.service;

import com.dropbox.core.InvalidAccessTokenException;
import com.google.gson.Gson;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onedatashare.server.model.core.*;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionCredential;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

public class DbxServiceTest {

    private static final String INVALID_ACCESS_TOKEN_MSG = "accessToken";
    private static final String INVALID_OAUTH_TOKEN = "49fad390491a5b547d0f782309b6a5b33f7ac087";

    @InjectMocks
    private DbxService dbxService;

    @Mock
    private UserService userService;

    @Mock
    private JobService jobService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenUnauthorizedUser_WhenCallingAnyServiceMethod_ShouldThrowException() {
        UUID uuid = UUID.randomUUID();
        UserAction userAction = userActionWithCredential(userActionCredentialWithId(uuid));
        User user = userWithId(uuid);
        String cookie = encodeIntoCookie("cookie", user);

        when(userService.getLoggedInUser(cookie)).thenReturn(just(user));

        Consumer<? super Stat> statConsumer = stat -> assertNull(stat.getFilesList());
        Consumer<? super Job> jobConsumer = Assert::assertNull;
        Consumer<Throwable> throwableConsumer =
                throwable -> assertTrue(
                        throwable instanceof InvalidAccessTokenException
                        && INVALID_ACCESS_TOKEN_MSG.equals(throwable.getMessage())
                );

        dbxService.list(cookie, userAction).subscribe(statConsumer, throwableConsumer);
        dbxService.delete(cookie, userAction).subscribe(aVoid -> {}, throwableConsumer);
        dbxService.mkdir(cookie, userAction).subscribe(aVoid -> {}, throwableConsumer);
        dbxService.submit(cookie, userAction).subscribe(jobConsumer, throwableConsumer);
    }

    private UserActionCredential userActionCredentialWithId(UUID uuid) {
        UserActionCredential uc = new UserActionCredential();
        uc.setUuid(uuid.toString());
        uc.setTokenSaved(true);
        return uc;
    }

    @NotNull
    private UserAction userActionWithCredential(UserActionCredential uc) {
        UserAction ua = new UserAction();
        ua.setUri(ODSConstants.DROPBOX_URI_SCHEME);
        ua.setCredential(uc);
        return ua;
    }

    private User userWithId(UUID uuid) {
        Map<UUID, Credential> map = new HashMap<>();
        OAuthCredential uic = new OAuthCredential(INVALID_OAUTH_TOKEN);
        map.put(uuid, uic);
        User u = new User();
        u.setHash("123");
        u.setFirstName("test_user");
        u.setCredentials(map);
        return u;
    }

    private String encodeIntoCookie(String cookieName, Object cookieValue) {
        String valueAsJson = new Gson().toJson(cookieValue);
        return ServerCookieEncoder.LAX.encode(cookieName, valueAsJson);
    }
}