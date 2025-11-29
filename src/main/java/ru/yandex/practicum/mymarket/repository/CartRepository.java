package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.model.Cart;

import org.springframework.data.domain.Pageable;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    @Query("SELECT * FROM сarts ORDER BY id DESC LIMIT :limit OFFSET :offset")
    Flux<Cart> findAllCart(int limit, int offset);
}
