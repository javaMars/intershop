package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.repository.UserRepository;
import ru.yandex.practicum.mymarket.security.CustomUserDetails;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            return userRepository.findByLogin(username)
                    .map(CustomUserDetails::new);
        };
    }

    @Bean
    public ServerAuthenticationFailureHandler failureHandler() {
        DefaultServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
        return (webFilterExchange, exception) -> {
            ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
            String errorUrl = "/login?error";
            if (exception.getMessage().contains("Bad credentials")) {
                errorUrl += "&reason=bad_credentials";
            }
            return redirectStrategy.sendRedirect(webFilterExchange.getExchange(), URI.create(errorUrl));
        };
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            AuthRedirectFilter authRedirectFilter
    ) {
        return http
                .addFilterBefore(authRedirectFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/css/**", "/js/**", "/items/**", "/register",
                                "/login", "/images/**", "/logout").permitAll()
                        .pathMatchers("/orders/**", "/buy", "/cart/**").hasRole("USER")
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(redirectAwareSuccessHandler())
                        .authenticationFailureHandler(failureHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((webFilterExchange, ex) -> {
                            ServerHttpResponse response = webFilterExchange.getResponse();
                            response.setStatusCode(HttpStatus.FORBIDDEN);
                            response.getHeaders().setContentType(MediaType.TEXT_HTML);
                            return response.writeWith(Mono.just(
                                    response.bufferFactory().wrap("Доступ запрещен!".getBytes())));
                        })
                )
                .build();
    }

    @Bean
    public ServerAuthenticationSuccessHandler redirectAwareSuccessHandler() {
        return (webFilterExchange, authentication) -> {
            ServerWebExchange exchange = webFilterExchange.getExchange();

            return exchange.getFormData()
                    .map(formData -> formData.getFirst("returnUrl"))
                    .filter(url -> url != null && !url.isEmpty())
                    .map(url -> URLDecoder.decode(url, StandardCharsets.UTF_8))
                    .switchIfEmpty(Mono.just("/items"))
                    .flatMap(targetUrl -> {
                        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                        exchange.getResponse().getHeaders().set("Location", targetUrl);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        return (exchange, authentication) -> {
            ServerWebExchange webExchange = exchange.getExchange();
            ServerHttpResponse response = webExchange.getResponse();
            response.setStatusCode(HttpStatus.SEE_OTHER);
            response.getHeaders().setLocation(URI.create("/items"));
            return response.setComplete();
        };
    }
}