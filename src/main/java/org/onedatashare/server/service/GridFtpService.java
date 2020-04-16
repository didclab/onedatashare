
package org.onedatashare.server.service;

import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.ODSConstants;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.gridftp.GridftpResource;
import org.onedatashare.server.module.gridftp.GridftpSession;
import org.onedatashare.server.service.oauth.GridFtpAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

@Service
public class GridFtpService extends ResourceService{

    @Autowired
    private UserService userService;

    @Autowired
    private GridFtpAuthService gridFtpAuthService;

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::stat);
    }

    public Mono<GridftpResource> getResourceWithUserUserAction(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.getUri());
        return userService.getLoggedInUser(cookie)
                .flatMap(user -> userService.getGlobusClient(null).map(client -> new GlobusWebClientCredential(userAction.getCredential().getGlobusEndpoint(), client)))
                .map(credential -> new GridftpSession(URI.create(userAction.getUri()), credential))
                .flatMap(GridftpSession::initialize)
                .flatMap(GridftpSession -> GridftpSession.select(path));
    }

    public Mono<Boolean> delete(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::deleteV2)
                .map(x -> true);
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<Boolean> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::mkdir)
                .map(x -> true);
    }

    public static String pathFromUri(String uri) {
        String path;
        if(uri.contains(ODSConstants.GRIDFTP_URI_SCHEME)){
            path = uri.substring(ODSConstants.GRIDFTP_URI_SCHEME.length());
        }
        else path = uri;
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public Mono<String> getOAuthUrl() {
        return Mono.fromSupplier(() -> gridFtpAuthService.start());
    }

    public Mono<String> completeOAuth(Map<String, String> queryParameters) {
        return gridFtpAuthService.finish(queryParameters)
                .flatMap(oauthCred -> {
                    oauthCred.setType(Credential.CredentialType.GLOBUS);
                    return userService.saveCredential(null, oauthCred);
                })
                .map(uuid -> "/oauth/uuid?identifier=" + uuid)
                .switchIfEmpty(Mono.just("/oauth/ExistingCredDropbox"));
    }
}
