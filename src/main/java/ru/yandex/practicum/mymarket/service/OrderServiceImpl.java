package ru.yandex.practicum.mymarket.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.*;
import ru.yandex.practicum.mymarket.model.*;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.CartRepository;

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
    private final CartRepository cartRepository;
    @Autowired
    private final CartItemRepository cartItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
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
            Cart cart = cartRepository.findAllCart(PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElse(null);
            //Cart cart = cartRepository.findById(1L).orElseThrow(() -> new RuntimeException("Корзина не найдена"));
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart);
            Order order = new Order();
            order.setItems(new ArrayList<>());
            long totalSum = 0L;

            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setItem(cartItem.getItem());
                orderItem.setCount(cartItem.getCount());
                orderItem.setOrder(order);
                order.getItems().add(orderItem);
                totalSum += cartItem.getItem().getPrice() * cartItem.getCount();
            }

            order.setTotalSum(totalSum);
            cartItemRepository.deleteAll();

            return orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

