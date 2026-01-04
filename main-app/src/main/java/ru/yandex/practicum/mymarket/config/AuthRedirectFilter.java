package ru.yandex.practicum.mymarket.config;

import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

@Component("authRedirectFilter")
@Order(1)
public class AuthRedirectFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path) || path.equals("/login")) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() != null)
                .defaultIfEmpty(false)
                .flatMap(hasAuth -> {
                    if (hasAuth) {
                        return chain.filter(exchange);
                    }

                    String currentUrl = request.getURI().getPath() +
                            (request.getURI().getQuery() != null ? "?" + request.getURI().getQuery() : "");
                    String loginUrl = "/login?returnUrl=" + URLEncoder.encode(currentUrl, StandardCharsets.UTF_8);

                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().set("Location", loginUrl);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/css") || path.startsWith("/js") ||
                path.startsWith("/register") || path.startsWith("/items");
    }
}