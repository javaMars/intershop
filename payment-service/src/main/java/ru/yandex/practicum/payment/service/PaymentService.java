package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.*;
import ru.yandex.practicum.payment.repository.BalanceRepository;

@Service
public class PaymentService {
    private final BalanceRepository balanceRepository;

    public PaymentService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public Mono<Balance> getBalance(String userId) {
        return balanceRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    AccountBalance defaultBalance = new AccountBalance(userId, 1000.0);
                    return balanceRepository.save(defaultBalance).thenReturn(defaultBalance);
                }))
                .map(account -> new Balance(account.getUserId(), account.getBalance()));
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return balanceRepository.findByUserId(request.getUserId())
                .switchIfEmpty(Mono.error(new RuntimeException("Пользователь не найден")))
                .flatMap(currentBalance -> {
                    if (currentBalance.getBalance() < request.getAmount()) {
                        return Mono.just(new PaymentResponse(false, request.getUserId(),
                                currentBalance.getBalance(), request.getOrderId()));
                    }
                    return balanceRepository.atomicDecrement(request.getUserId(), request.getAmount())
                            .map(newBalance -> new PaymentResponse(true, request.getUserId(),
                                    newBalance.getBalance(), request.getOrderId()))
                            .switchIfEmpty(Mono.error(new RuntimeException("Ошибка списания средств")));
                });
    }
}