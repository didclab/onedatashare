//package org.onedatashare.server.controller;
//
//import org.springframework.boot.autoconfigure.web.ResourceProperties;
//import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
//import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
//import org.springframework.boot.web.reactive.error.ErrorAttributes;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.server.*;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebExceptionHandler;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//
//@Component
//class ExceptionHandler extends WebExceptionHandler {
//        @Override public Mono<Void> handle(ServerWebExchange exchange,  Throwable ex) {
//        /* Handle different exceptions here */
//            if(ex != null){
//
//            }
//
//
//        /* Do common thing like logging etc... */
//
//            return Mono.empty();
//        }
//}