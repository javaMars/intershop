package ru.yandex.practicum.mymarket.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.model.*;

@Service
public class PaymentServiceClient {
    private final WebClient paymentWebClient;
    private final TokenService tokenService;

    private static final String BASE_PATH = "/api/payments";

    public PaymentServiceClient(WebClient paymentWebClient, TokenService tokenService) {
        this.paymentWebClient = paymentWebClient;
        this.tokenService = tokenService;
    }

    // POST /api/payments/pay
    public Mono<PaymentResponse> pay(PaymentRequest request) {
        return tokenService.getPaymentServiceToken()
                .flatMap(token -> paymentWebClient.post()
                        .uri(BASE_PATH + "/pay")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(PaymentResponse.class));
    }

    // POST /api/payments/initBalance
    public Mono<Balance> initBalance(String userId, Double initialBalance) {
        InitBalanceRequest request = new InitBalanceRequest()
                .userId(userId)
                .initialBalance(initialBalance);

        return tokenService.getPaymentServiceToken()
                .flatMap(token -> paymentWebClient.post()
                        .uri("/api/payments/initBalance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Balance.class)
                        .onErrorReturn(new Balance().userId(userId).balance(initialBalance)));
    }

    // GET /api/payments/getBalance/{userId}
    public Mono<Balance> getBalance(String userId) {
        return tokenService.getPaymentServiceToken()
                .flatMap(token -> paymentWebClient.get()
                        .uri(BASE_PATH + "/getBalance/{userId}", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Balance.class)
                        .onErrorResume(WebClientResponseException.NotFound.class, ex ->
                                Mono.just(new Balance().balance(0.0).userId(userId))
                        ));
    }
}