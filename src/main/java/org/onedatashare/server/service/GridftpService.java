
package org.onedatashare.server.service;

import org.onedatashare.module.globusapi.Result;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.GlobusWebClientCredential;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.model.useraction.UserActionResource;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.onedatashare.server.module.gridftp.GridftpResource;
import org.onedatashare.server.module.gridftp.GridftpSession;
import org.onedatashare.server.module.vfs.VfsResource;
import org.onedatashare.server.module.vfs.VfsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class GridftpService {

    @Autowired
    private UserService userService;

    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction).flatMap(GridftpResource::stat);
    }

    public Mono<GridftpResource> getResourceWithUserUserAction(String cookie, UserAction userAction) {
        final String path = pathFromUri(userAction.uri);
        return userService.getLoggedInUser(cookie)
            .flatMap(user -> userService.getGlobusClient(cookie).map(client -> new GlobusWebClientCredential(userAction.getCredential().getGlobusEndpoint(), client)))
            .map(credential -> new GridftpSession(URI.create(userAction.uri), credential))
            .flatMap(GridftpSession::initialize)
            .flatMap(GridftpSession -> GridftpSession.select(path));
    }

    public Mono<Result> delete(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::deleteV2);
    }

    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return getResourceWithUserUserAction(cookie, userAction)
                .flatMap(GridftpResource::mkdir)
                .flatMap(GridftpResource::stat);
    }

    public static String pathFromUri(String uri) {
        String path;
        if(uri.contains("gsiftp://")){
            path = uri.split("gsiftp://")[1];
        }
        else path = uri;
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }
}
