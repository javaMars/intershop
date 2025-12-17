package ru.yandex.practicum.payment.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.AccountBalance;

public interface BalanceRepository extends R2dbcRepository<AccountBalance, String> {
    Mono<AccountBalance> findByUserId(String userId);
}
