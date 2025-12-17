package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    @Query("SELECT * FROM carts ORDER BY id DESC LIMIT 1")
    Mono<Cart> findLastCart();
}
