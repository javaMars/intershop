package ru.yandex.practicum.payment.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.Balance;

public interface BalanceRepository extends R2dbcRepository<Balance, String> {
    @Query("SELECT user_id, balance FROM balance WHERE user_id = :userId")
    Mono<Balance> findByUserId(@Param("userId") String userId);

    @Query("""
    UPDATE balance 
    SET balance = balance - $2 
    WHERE user_id = $1 AND balance >= $2 
    RETURNING user_id, balance
    """)
    Mono<Balance> atomicDecrement(String userId, Double amount);

}