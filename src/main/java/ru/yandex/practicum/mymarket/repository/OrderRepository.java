package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;

public interface OrderRepository extends R2dbcRepository<Order, Long> {
    @Query("""
        SELECT o.*, oi.id as oi_id, oi.order_id, oi.item_id, oi.count 
        FROM orders o 
        LEFT JOIN order_items oi ON o.id = oi.order_id 
        WHERE o.id = :id
        """)
    Mono<Order> findByIdWithItems(Long id);
}
