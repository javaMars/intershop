package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.*;


@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Mono<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    Flux<CartItem> findByCartId(Long cartId);

    @Query("DELETE FROM cart_items ci WHERE ci.cart_id = :cartId AND ci.item_id = :itemId")
    Mono<Long> deleteByCartIdAndItemId(Long cartId, Long itemId);
}
