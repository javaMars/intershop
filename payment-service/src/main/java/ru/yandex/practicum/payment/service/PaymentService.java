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
                    Balance defaultBalance = new Balance(userId, 10000.0);
                    return balanceRepository.save(defaultBalance).thenReturn(defaultBalance);
                }))
                .map(account -> new Balance(account.getUserId(), account.getBalance()));
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return balanceRepository.atomicDecrement(request.getUserId(), request.getAmount())
                .map(newBalance -> new PaymentResponse(true, request.getUserId()));
    }

    public Mono<Balance> initUserBalance(String userId, Double initialBalance) {

        Double balanceToSet = (initialBalance != null) ? initialBalance : 10000.0;

        return balanceRepository.findByUserId(userId)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalStateException("Пользователь уже существует"));
                    }
                    Balance balance = new Balance(userId, balanceToSet);
                    return balanceRepository.save(balance);
                });
    }
}