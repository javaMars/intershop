package ru.yandex.practicum.mymarket.context;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserContext {
    public static final String DEFAULT_USER_ID = "user123";

    public static Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }

    public static Mono<String> getDefaultUserId() {
        return Mono.just(DEFAULT_USER_ID);
    }
}
