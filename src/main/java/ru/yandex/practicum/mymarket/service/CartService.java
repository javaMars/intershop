package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.CartItem;

import java.util.List;
import java.util.Map;

public interface CartService {
    Flux<CartItem> findCartItems();
    Mono<Integer> getItemCountInCart(Long itemId);
    Mono<List<ItemDto>> findCartItemsWithDetails();
    Mono<Map<Long, Integer>> getItemCountsMap();
    Mono<Void> handleItemAction(Long itemId, String action);
}
