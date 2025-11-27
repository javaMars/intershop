package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.model.CartItem;

import java.util.List;
import java.util.Map;

public interface CartService {
    List<CartItem> findCartItems();
    int getItemCountInCart(Long itemId);
    Map<Long, Integer> getItemCountsMap();
    void handleItemAction(Long itemId, String action);
}
