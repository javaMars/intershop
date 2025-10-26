package ru.yandex.practicum.mymarket.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.*;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public List<OrderDto> findAllOrders() {
        try {
            return orderRepository.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setTotalSum(order.getTotalSum());

        List<ItemDto> items = order.getItems() != null
                ? order.getItems().stream()
                .map(this::convertOrderItemToItemDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        dto.setItems(items);
        return dto;
    }

    private ItemDto convertOrderItemToItemDto(OrderItem orderItem) {
        Item item = orderItem.getItem();
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setTitle(item.getTitle());
        itemDto.setDescription(item.getDescription());
        itemDto.setImgPath(item.getImgPath());
        itemDto.setPrice(item.getPrice());
        itemDto.setCount(orderItem.getCount());
        return itemDto;
    }

    public Optional<OrderDto> findOrder(Long id) {
        return orderRepository.findById(id).map(this::convertToDto);
    }

    @Transactional
    public Order createFromCart() {
        try {
            List<Item> cartItems = productRepository.findItemsInCart();
            Order order = new Order();
            order.setItems(new ArrayList<>());
            long totalSum = 0L;

            for (Item item : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setItem(item);
                orderItem.setCount(item.getCount());
                orderItem.setOrder(order); // ВАЖНО: свяжите OrderItem с Order
                order.getItems().add(orderItem);
                totalSum += item.getPrice() * item.getCount();
            }

            order.setTotalSum(totalSum);
            removeItemsFromCart();

            return orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void removeItemsFromCart() throws Exception {
        try {
            productRepository.removeItemsFromCart();
        } catch (DataAccessException e) {
            throw new Exception("Не удалось обновить корзину", e);
        }
    }
}

