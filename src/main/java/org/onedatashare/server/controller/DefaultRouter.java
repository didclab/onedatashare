//package org.onedatashare.server.controller;
//
//import org.onedatashare.server.model.core.Credential;
//import org.onedatashare.server.model.core.Job;
//import org.onedatashare.server.model.useraction.UserAction;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.http.MediaType;
//import org.springframework.http.codec.ServerCodecConfigurer;
//import org.springframework.web.reactive.config.CorsRegistry;
//import org.springframework.web.reactive.config.EnableWebFlux;
//import org.springframework.web.reactive.config.ViewResolverRegistry;
//import org.springframework.web.reactive.config.WebFluxConfigurer;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.web.reactive.function.BodyInserters.fromObject;
//import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
//import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
//import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//import static org.springframework.web.reactive.function.server.ServerResponse.ok;
//
//
//@Configuration
//@EnableWebFlux
//public class DefaultRouter implements WebFluxConfigurer {
//
//    @Autowired
//    private CancelController cancel;
//
//    @Autowired
//    private CredController cred;
//
//    @Autowired
//    private ListController list;
//    //done
//    @Autowired
//    private DeleteController delete;
//
//    @Autowired
//    private MkdirController mkdir;
//
//    @Autowired
//    private OauthController oauth;
//
//    @Autowired
//    private QueueController queue;
//
//    @Autowired
//    private RestartJobController restart;
//
//    @Autowired
//    private SubmitController submit;
//
//    @Autowired
//    private UserController user;
//
//
//    @Bean
//    public RouterFunction<ServerResponse> nestedRoutes() {
//        return route(GET("/api/stork/oauth"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body( fromObject(oauth.handle(req.headers().asHttpHeaders(), req.queryParams().toSingleValueMap()))))
//                .andRoute(GET("/api/stork/cred"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body( fromObject(cred.listCredentials(req.headers().asHttpHeaders(), req.queryParams().toSingleValueMap()))))
//                .andRoute(GET("/api/stork/user"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body( fromObject(user.getHistory(req.headers().asHttpHeaders()))))
//                .andRoute(POST("/api/stork/user"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body( fromObject(req.bodyToMono(UserAction.class)
//                            .map(userAction ->  user.performAction(
//                                req.headers().asHttpHeaders(), userAction)))))
//                .andRoute(POST("/api/stork/cancel"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(req.bodyToMono(UserAction.class)
//                        .flatMap(userAction -> (Mono<Job>) cancel
//                            .cancel(req.headers().asHttpHeaders(), userAction)), Job.class))
//                .andRoute(POST("/api/stork/ls"),
//                    req -> ok()
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body( fromObject(req.bodyToMono(UserAction.class)
//                            .map(userAction -> list
//                                .list(req.headers().asHttpHeaders(), userAction)))))
//                .andRoute(POST("/api/stork/delete"),
//                        req -> ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .body( fromObject(req.bodyToMono(UserAction.class)
//                                        .map(userAction -> delete
//                                                .delete(req.headers().asHttpHeaders(), userAction)))))
//                .andRoute(POST("/api/stork/mkdir"),
//                        req -> ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .body( fromObject(req.bodyToMono(UserAction.class)
//                                        .map(userAction ->  mkdir
//                                                .mkdir(req.headers().asHttpHeaders(), userAction)))))
//                .andRoute(POST("/api/stork/queue"),
//                        req -> ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .body(fromObject(
//                                        queue.queue(req.headers().asHttpHeaders())
//                                )))
//                .andRoute(POST("/api/stork/restart"),
//                        req -> ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .body( fromObject(req.bodyToMono(UserAction.class)
//                                        .map(userAction ->  restart
//                                                .restartJob(req.headers().asHttpHeaders(), userAction)))))
//                .andRoute(POST("/api/stork/submit"),
//                        req -> ok()
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .body( fromObject(req.bodyToMono(UserAction.class)
//                                        .map(userAction ->  submit.submit(
//                                                req.headers().asHttpHeaders(), userAction)))));
//    }
//    @Bean
//    RouterFunction<ServerResponse> staticResourceRouter(){
//        return RouterFunctions.resources("/transfer**", new ClassPathResource("static/index.html"));
//    }
//
//    @Bean
//    RouterFunction<ServerResponse> staticResourceRouter2(){
//        return RouterFunctions.resources("/login**", new ClassPathResource("static/index.html"));
//    }
//
//    @Bean
//    RouterFunction<ServerResponse> staticResourceRouter3(){
//        return RouterFunctions.resources("/", new ClassPathResource("static/index.html"));
//    }
//    @Bean
//    RouterFunction<ServerResponse> staticResourceRouter4(){
//        return RouterFunctions.resources("/**", new ClassPathResource("static/"));
//    }
////    @Bean
////    RouterFunction<ServerResponse> staticResourceRouter2(){
////        return RouterFunctions.resources("/static/**", new ClassPathResource("static/static/"));
////
////    }
//
//
//
//    @Override
//    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
//        // configure message conversion...
//    }
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // configure CORS...
//    }
//
//    @Override
//    public void configureViewResolvers(ViewResolverRegistry registry) {
//        // configure view resolution for HTML rendering...
//    }
//}