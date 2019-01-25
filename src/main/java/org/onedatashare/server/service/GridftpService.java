//package org.onedatashare.server.service;
//
//import org.onedatashare.server.model.core.Stat;
//import org.onedatashare.server.model.credential.UserInfoCredential;
//import org.onedatashare.server.model.useraction.UserAction;
//import org.onedatashare.server.model.useraction.UserActionResource;
//import org.onedatashare.server.module.gridftp.GridftpResource;
//import org.onedatashare.server.module.gridftp.GridftpSession;
//import org.onedatashare.server.module.vfs.VfsResource;
//import org.onedatashare.server.module.vfs.VfsSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URI;
//
//@Service
//public class GridftpService {
//    @Autowired
//    private UserService userService;
//
//    public Mono<Stat> list(String cookie, UserAction userAction) {
//        return getResourceWithUserActionUri(cookie, userAction).flatMap(GridftpResource::stat);
//    }
//
//    public Mono<GridftpResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
//        final String path = pathFromUri(userAction.uri);
//        return userService.getLoggedInUser(cookie)
//                .map(user -> new UserInfoCredential(userAction.credential))
//                .map(credential -> new GridftpSession(URI.create(userAction.uri), credential))
//                .flatMap(GridftpSession::initialize)
//                .flatMap(GridftpSession -> GridftpSession.select(path));
//    }
//
//    public String pathFromUri(String uri) {
//        String path;
//        if(uri.contains("gsiftp://")){
//            path = uri.split("gsiftp://")[1];
//        }
//        else path = uri;
//        try {
//            path = java.net.URLDecoder.decode(path, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return path;
//    }
//}
