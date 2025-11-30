package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

import java.util.Map;

public interface CartService {
    Flux<CartItem> findCartItems();
    Mono<Integer> getItemCountInCart(Long itemId);
    Mono<Map<Long, Integer>> getItemCountsMap();
    Mono<Void> handleItemAction(Long itemId, String action);
}
