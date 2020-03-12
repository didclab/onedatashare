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
import org.onedatashare.server.module.dropbox.DbxResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;
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
        Consumer<Boolean> rsrcConsumer = Assert::assertFalse;
        Consumer<? super Job> jobConsumer = Assert::assertNull;
        Consumer<Throwable> throwableConsumer =
                throwable -> assertTrue(
                        throwable instanceof InvalidAccessTokenException
                        && INVALID_ACCESS_TOKEN_MSG.equals(throwable.getMessage())
                );

        dbxService.list(cookie, userAction).subscribe(statConsumer, throwableConsumer);
        dbxService.delete(cookie, userAction).subscribe(rsrcConsumer, throwableConsumer);
        dbxService.mkdir(cookie, userAction).subscribe(rsrcConsumer, throwableConsumer);
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