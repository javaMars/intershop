package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.OrderDto;

import java.util.List;

public interface OrderService {
    Mono<OrderDto> findOrder(Long id);
    Mono<OrderDto> createFromCart(String userId);
    Mono<Void> cancelOrder(Long orderId);
    Mono<Void> clearCartAfterPayment(String userId);
    Flux<OrderDto> findOrdersByUserId(String userId);
}
