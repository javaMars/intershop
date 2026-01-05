package ru.yandex.practicum.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.*;
import ru.yandex.practicum.payment.service.PaymentService;

import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/getBalance/{userId}")
    public Mono<ResponseEntity<Balance>> getBalance(
            @PathVariable String userId,
            @AuthenticationPrincipal Jwt jwt) {

        String clientId = jwt.getClaimAsString("client_id");
        if (!"payment-service-client".equals(clientId)) {
            return Mono.just(ResponseEntity.status(403).build());
        }

        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/pay")
    public Mono<ResponseEntity<PaymentResponse>> pay(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest()
                        .body(new PaymentResponse(false, request.getUserId()))
                ));
    }

    @PostMapping("/initBalance")
    public Mono<ResponseEntity<Balance>> initBalance(
            @RequestBody Mono<InitBalanceRequest> requestMono,
            @AuthenticationPrincipal Jwt jwt) {
        String clientId = jwt.getClaimAsString("client_id");

        if (!"payment-service-client".equals(clientId)) {
            return Mono.just(ResponseEntity.status(403).build());
        }

        return requestMono
                .flatMap(request ->
                    paymentService.initUserBalance(request.getUserId(), request.getInitialBalance())
                )
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest()
                        .body(new Balance().userId("unknown").balance(0.0)));
    }
}