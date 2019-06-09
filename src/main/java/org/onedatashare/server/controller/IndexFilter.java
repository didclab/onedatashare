package org.onedatashare.server.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class IndexFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (exchange.getRequest().getURI().getPath().equals("/") ||
            exchange.getRequest().getURI().getPath().equals("/transfer") ||
            exchange.getRequest().getURI().getPath().equals("/user") ||
            exchange.getRequest().getURI().getPath().equals("/queue") ||
            exchange.getRequest().getURI().getPath().equals("/clientsInfo") ||
            exchange.getRequest().getURI().getPath().equals("/history") ||
            exchange.getRequest().getURI().getPath().startsWith("/account") ||
            exchange.getRequest().getURI().getPath().startsWith("/oauth") ||
            exchange.getRequest().getURI().getPath().equals("/support")
      ) {
      return chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path("/index.html").build()).build());
    }
    return chain.filter(exchange);
  }

}
