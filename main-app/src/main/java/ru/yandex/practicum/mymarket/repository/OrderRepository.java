package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.model.Order;

public interface OrderRepository extends R2dbcRepository<Order, Long> {
    @Query("SELECT * FROM orders WHERE user_id = :userId")
    Flux<Order> findByUserId(String userId);
}
