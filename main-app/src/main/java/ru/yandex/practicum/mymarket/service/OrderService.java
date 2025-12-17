package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.OrderDto;

public interface OrderService {
    Flux<OrderDto> findAllOrders();
    Mono<OrderDto> findOrder(Long id);
    Mono<OrderDto> createFromCart();
    Mono<Void> cancelOrder(Long orderId);
}
