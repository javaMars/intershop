package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.*;
import ru.yandex.practicum.mymarket.model.*;
import ru.yandex.practicum.mymarket.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Flux<OrderDto> findAllOrders() {
        return orderRepository.findAll()
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(orderItems -> convertToDto(order, orderItems))  // ← flatMap вместо map
                )
                .onErrorResume(e -> {
                    System.err.println("Ошибка при получении заказов: " + e.getMessage());
                    return Flux.empty();
                });
    }



    private Mono<OrderDto> convertToDto(Order order, List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) {
            OrderDto dto = new OrderDto();
            dto.setId(order.getId());
            dto.setTotalSum(order.getTotalSum());
            dto.setItems(Collections.emptyList());
            return Mono.just(dto);
        }

        List<Long> itemIds = orderItems.stream()
                .map(OrderItem::getItemId)
                .collect(Collectors.toList());

        return productRepository.findAllById(itemIds)
                .collectMap(Item::getId, product -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("price", product.getPrice());
                    data.put("title", product.getTitle());
                    return data;
                })
                .flatMap(priceMap -> Flux.fromIterable(orderItems)
                        .flatMap(item -> {
                            OrderItemDto dto = new OrderItemDto();
                            dto.setId(item.getId());
                            dto.setItemId(item.getItemId());
                            dto.setCount(item.getCount());

                            Map<String, Object> productData = priceMap.get(item.getItemId());
                            if (productData != null) {
                                dto.setPrice((Long) productData.get("price"));
                                dto.setTitle((String) productData.get("title"));
                            }
                            return Mono.just(dto);
                        })
                        .collectList()
                )
                .map(itemDtos -> {
                    OrderDto dto = new OrderDto();
                    dto.setId(order.getId());
                    dto.setTotalSum(order.getTotalSum());
                    dto.setItems(itemDtos);
                    return dto;
                });
    }

    private Mono<OrderItemDto> convertToItemDto(OrderItem item) {
        return productRepository.findById(item.getItemId())
                .map(product -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setId(item.getId());
                    dto.setItemId(item.getItemId());
                    dto.setCount(item.getCount());
                    dto.setPrice(product.getPrice());  // добавляем
                    dto.setTitle(product.getTitle());   // добавляем
                    return dto;
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    OrderItemDto dto = new OrderItemDto();
                    dto.setId(item.getId());
                    dto.setItemId(item.getItemId());
                    dto.setCount(item.getCount());
                    return dto;
                }));
    }

    public Mono<OrderDto> findOrder(Long id) {
        return orderRepository.findById(id)
                .flatMap(order ->
                        orderItemRepository.findByOrderId(id)
                                .collectList()
                                .flatMap(orderItems -> convertToDto(order, orderItems))
                )
                .onErrorResume(e -> {
                    return Mono.empty();
                });
    }
    public Mono<Order> createFromCart() {
        return cartRepository.findLastCart()
                .switchIfEmpty(Mono.error(new RuntimeException("Корзина не найдена")))
                .flatMap(cart ->
                        cartItemRepository.findByCartId(cart.getId())
                                .map(cartItem -> {
                                    OrderItem orderItem = new OrderItem();
                                    orderItem.setItemId(cartItem.getItemId());
                                    orderItem.setCount(cartItem.getCount());
                                    return orderItem;
                                })
                                .collectList()
                                .flatMap(orderItems -> {
                                    if (orderItems.isEmpty()) {
                                        return Mono.error(new RuntimeException("Корзина пуста"));
                                    }
                                    return createOrderFromItems(orderItems, cart.getId());
                                })
                );
    }

    private Mono<Order> createOrderFromItems(List<OrderItem> orderItems, Long cartId) {
        return calculateTotalSum(orderItems)
                .map(totalSum -> {
                    Order order = new Order();
                    order.setTotalSum(totalSum);
                    return order;
                })
                .flatMap(orderRepository::save)
                .flatMap(savedOrder ->
                        Flux.fromIterable(orderItems)
                                .doOnNext(oi -> oi.setOrderId(savedOrder.getId()))
                                .flatMap(orderItemRepository::save)
                                .then(Mono.just(savedOrder))
                )
                .flatMap(savedOrder ->
                        cartItemRepository.deleteByCartId(cartId)
                                .thenReturn(savedOrder)
                );
    }

    private Mono<Long> calculateTotalSum(List<OrderItem> orderItems) {
        return Flux.fromIterable(orderItems)
                .concatMap(oi -> productRepository.findById(oi.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + oi.getItemId())))
                        .map(item -> (long) item.getPrice() * oi.getCount()))
                .reduce(0L, Long::sum);
    }
}