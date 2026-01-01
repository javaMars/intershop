package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    @Query("SELECT * FROM carts WHERE user_id = :userId ORDER BY id DESC LIMIT 1")
    Mono<Cart> findLastCartByUserId(@Param("userId") String userId);

    @Query("DELETE FROM carts WHERE user_id = :userId")
    Mono<Void> deleteByUserId(@Param("userId") String userId);
}
