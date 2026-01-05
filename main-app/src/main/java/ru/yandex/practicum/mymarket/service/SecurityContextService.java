package ru.yandex.practicum.mymarket.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

@Service
public class SecurityContextService {

    public Mono<Void> addSecurityAttributes(Model model) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    boolean isAuth = auth != null && auth.isAuthenticated();
                    String username = auth != null ? auth.getName() : null;

                    model.addAttribute("isAuthenticated", isAuth);
                    model.addAttribute("username", username);
                })
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    model.addAttribute("isAuthenticated", false);
                    model.addAttribute("username", null);
                }))
                .then();
    }
}