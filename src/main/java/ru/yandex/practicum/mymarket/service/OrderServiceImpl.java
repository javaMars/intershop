package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.*;
import ru.yandex.practicum.mymarket.model.*;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.CartRepository;

import java.util.Collections;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final CartRepository cartRepository;
    @Autowired
    private final CartItemRepository cartItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Flux<OrderDto> findAllOrders() {
        return orderRepository.findAll()
                .flatMap(this::convertToDto)
                .onErrorResume(e -> Flux.empty());
    }

    private Mono<OrderDto> convertToDto(Order order) {
        return Flux.fromIterable(order.getItems() != null ? order.getItems() : Collections.emptyList())
                .flatMap(this::convertOrderItemToItemDto)
                .collectList()
                .map(items -> {
                    OrderDto dto = new OrderDto();
                    dto.setId(order.getId());
                    dto.setTotalSum(order.getTotalSum());
                    dto.setItems(items);
                    return dto;
                });
    }

    private Mono<ItemDto> convertOrderItemToItemDto(OrderItem orderItem) {

        Item item = orderItem.getItem();
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setTitle(item.getTitle());
        itemDto.setDescription(item.getDescription());
        itemDto.setImgPath(item.getImgPath());
        itemDto.setPrice(item.getPrice());
        itemDto.setCount(orderItem.getCount());

        return Mono.just(itemDto);
    }

    public Mono<OrderDto> findOrder(Long id) {
        return orderRepository.findById(id).flatMap(this::convertToDto);
    }

    @Transactional
    public Mono<Order> createFromCart() {
        return cartRepository.findLastCart()
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItem -> {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setItem(cartItem.getItem());
                            orderItem.setCount(cartItem.getCount());
                            return Mono.just(orderItem);
                        })
                        .collectList()
                        .flatMap(orderItems -> {
                            Order order = new Order();
                            order.setItems(orderItems);
                            long totalSum = orderItems.stream()
                                    .mapToLong(oi -> oi.getItem().getPrice() *
                                            oi.getCount())
                                    .sum();
                            order.setTotalSum(totalSum);

                            return cartItemRepository.deleteAll()
                                    .then(orderRepository.save(order));
                        })
                )
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка при создании заказа: " + e.getMessage())));
    }
}