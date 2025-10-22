package ru.yandex.practicum.mymarket.service;

import jakarta.transaction.Transactional;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.dto.Order;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderDto> findAllOrders() {

        return orderRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private OrderDto convertToDto(Order order) {
        List<Item> items = order.getItems().stream()
                .map(item -> new Item(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getImgPath(),
                        item.getPrice(),
                        item.getCount()
                ))
                .collect(Collectors.toList());

        long totalSum = items.stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
                .sum();

        return new OrderDto(order.getId(), items, totalSum);
    }

    public Optional<OrderDto> findOrder(Long id) {
        return orderRepository.findById(id).map(this::convertToDto);
    }
    @Transactional
    public Order createFromCart(){
        List<Item> items = productRepository.findItemsInCart();
        Order order = new Order(items);
        orderRepository.save(order);

        return order;
    }
}

