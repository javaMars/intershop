package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.*;
import ru.yandex.practicum.payment.repository.BalanceRepository;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    private final BalanceRepository balanceRepository;

    public PaymentService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public Mono<Balance> getBalance(String userId) {
        return balanceRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    AccountBalance defaultBalance = new AccountBalance(userId, 1000.0, LocalDateTime.now());
                    return balanceRepository.save(defaultBalance).thenReturn(defaultBalance);
                }))
                .map(acc -> new Balance(acc.getUserId(), acc.getBalance()));
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        String userId = request.getUserId();
        return getBalance(userId)
                .filter(response -> response.getBalance() >= request.getAmount())
                .flatMap(response -> {
                    double newBalance = response.getBalance() - request.getAmount();
                    AccountBalance updated = new AccountBalance(
                            userId, newBalance, LocalDateTime.now()
                    );
                    return balanceRepository.save(updated)
                            .thenReturn(new PaymentResponse(true, userId, newBalance, request.getOrderId()));
                })
                .switchIfEmpty(Mono.just(new PaymentResponse(false, userId, 0D, request.getOrderId())));
    }
}
