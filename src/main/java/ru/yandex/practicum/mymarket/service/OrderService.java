package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.dto.OrderDto;

public interface OrderService {
    Flux<OrderDto> findAllOrders();
    Mono<OrderDto> findOrder(Long id);
    Mono<Order> createFromCart();
}
