package org.onedatashare.server.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.onedatashare.server.model.core.Job;
import org.onedatashare.server.model.core.Stat;
import org.onedatashare.server.model.credential.UserInfoCredential;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.http.HttpResource;
import org.onedatashare.server.module.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@Service
public class HttpFileService implements ResourceService<HttpResource> {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    public Mono<HttpResource> getResourceWithUserActionUri(String cookie, UserAction userAction) {
        String path = pathFromUri(userAction.uri);
        return userService.getLoggedInUser(cookie)
                .map(user -> new HttpSession(URI.create(userAction.uri)))
                .flatMap(HttpSession::initialize)
                .flatMap(httpSession -> httpSession.select(path));
    }

    private String pathFromUri(String uri) {
        String path = "";
        if(uri.contains("http://")){
            path = uri;
        }
        try {
            path = java.net.URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    @Override
    public Mono<Stat> list(String cookie, UserAction userAction) {
        return getResourceWithUserActionUri(cookie, userAction).flatMap(HttpResource::stat);
    }

    @Override
    /* Not allowed */
    public Mono<Stat> mkdir(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    /* Not allowed */
    public Mono<HttpResource> delete(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    public Mono<Job> submit(String cookie, UserAction userAction) {
        return null;
    }

    @Override
    /* TODO: handle at the front-end */
    public Mono<String> download(String cookie, UserAction userAction) {
        return null;
    }
}
