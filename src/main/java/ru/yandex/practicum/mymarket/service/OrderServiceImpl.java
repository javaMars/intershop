package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.*;
import ru.yandex.practicum.mymarket.model.*;
import ru.yandex.practicum.mymarket.repository.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final OrderItemRepository orderItemRepository;
    @Autowired
    private final CartRepository cartRepository;
    @Autowired
    private final CartItemRepository cartItemRepository;
    @Autowired
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
                                .collectList()  // Получаем List<OrderItem>
                                .flatMap(orderItems -> convertToDto(order, orderItems))  // Передаем оба объекта
                )
                .onErrorResume(e -> {
                    System.err.println("Ошибка при получении заказа " + id + ": " + e.getMessage());
                    return Mono.empty();
                });
    }


    public Mono<Order> createFromCart() {

        return cartRepository.findLastCart()
                .switchIfEmpty(Mono.error(new RuntimeException("Корзина не найдена")))
                .flatMap(cart -> {
                    System.out.println("Обработка корзины ID: " + cart.getId());

                    return cartItemRepository.findByCartId(cart.getId())
                            .doOnNext(cartItem -> System.out.println("Товар из корзины: itemId=" + cartItem.getItemId()))
                            .map(cartItem -> {
                                OrderItem orderItem = new OrderItem();
                                orderItem.setItemId(cartItem.getItemId());
                                orderItem.setCount(cartItem.getCount());
                                return orderItem;
                            })
                            .collectList()
                            .switchIfEmpty(Mono.error(new RuntimeException("Корзина пуста")))
                            .flatMap(orderItems -> {
                                System.out.println("Найдено товаров для заказа: " + orderItems.size());

                                Order order = new Order();

                                return calculateTotalSum(orderItems)
                                        .doOnError(err -> System.err.println("Ошибка расчёта суммы: " + err.getMessage()))
                                        .doOnNext(sum -> {
                                            System.out.println("Итоговая сумма заказа: " + sum);
                                            order.setTotalSum(sum);
                                        })
                                        .flatMap(sum -> orderRepository.save(order))
                                        .doOnSuccess(saved -> System.out.println("Заказ сохранён: ID=" + saved.getId()))
                                        .doOnError(err -> System.err.println("Ошибка сохранения заказа: " + err.getMessage()))
                                        .flatMap(savedOrder -> {
                                            Flux<OrderItem> savedItems = Flux.fromIterable(orderItems)
                                                    .doOnNext(oi -> oi.setOrderId(savedOrder.getId()))
                                                    .flatMap(orderItemRepository::save)
                                                    .doOnError(err -> System.err.println("Ошибка сохранения OrderItem: " + err.getMessage()));

                                            return savedItems
                                                    .then(Mono.just(savedOrder))
                                                    .doOnError(err -> System.err.println("Ошибка в savedItems: " + err.getMessage()));
                                        })
                                        .flatMap(savedOrder ->
                                                cartItemRepository.deleteByCartId(cart.getId())
                                                        .doOnError(err -> System.err.println("Ошибка очистки корзины: " + err.getMessage()))
                                                        .thenReturn(savedOrder)
                                        );
                            });
                });
    }

    private Mono<Long> calculateTotalSum(List<OrderItem> orderItems) {
        return Flux.fromIterable(orderItems)
                .concatMap(oi -> productRepository.findById(oi.getItemId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден: " + oi.getItemId())))
                        .map(item -> (long) item.getPrice() * oi.getCount()))
                .reduce(0L, Long::sum);
    }

}