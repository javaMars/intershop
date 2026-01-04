package ru.yandex.practicum.payment.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.AccountBalance;

public interface BalanceRepository extends R2dbcRepository<AccountBalance, String> {
    Mono<AccountBalance> findByUserId(String userId);

    @Query("UPDATE account_balance SET balance = GREATEST(0, balance - :amount), " +
            "date_update = CURRENT_TIMESTAMP " +
            "WHERE user_id = :userId AND balance >= :amount " +
            "RETURNING *")
    Mono<AccountBalance> atomicDecrement(
            @Param("userId") String userId,
            @Param("amount") Double amount
    );
}