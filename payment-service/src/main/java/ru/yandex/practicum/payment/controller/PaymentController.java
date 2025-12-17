package ru.yandex.practicum.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/getBalance/{userId}")
    public Mono<ResponseEntity<Balance>> getBalance(@PathVariable String userId) {
        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping("/pay")
    public Mono<ResponseEntity<PaymentResponse>> pay(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request)
                .map(ResponseEntity::ok)  // 200 при успехе
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest()
                        .body(new PaymentResponse(false, request.getUserId(), 0D, request.getOrderId()))
                ));
    }
}
