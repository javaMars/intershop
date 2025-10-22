package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.Order;
import ru.yandex.practicum.mymarket.dto.OrderDto;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderDto> findAllOrders();
    Optional<OrderDto> findOrder(Long id);
    Order createFromCart();
}
