package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Cart;

import java.util.List;
import java.util.Map;

public interface CartService {
    Mono<Integer> getItemCountInCart(Long itemId);
    Mono<List<ItemDto>> findCartItemsWithDetails();
    Mono<Map<Long, Integer>> getItemCountsMap();
    Mono<Void> handleItemAction(Long itemId, String action);
    Mono<Cart> findLastCart();
}
