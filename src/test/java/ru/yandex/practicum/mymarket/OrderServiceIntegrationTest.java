package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.repository.*;
import ru.yandex.practicum.mymarket.service.CartServiceImpl;
import ru.yandex.practicum.mymarket.service.OrderServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
public class OrderServiceIntegrationTest {
    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        orderItemRepository.deleteAll().block();
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        productRepository.deleteAll().block();
    }

    @Test
    void createFromCart_shouldCreateOrderWithItems() {
        Item item = new Item();
        item.setTitle("Ноутбук");
        item.setPrice(50000L);
        Item savedProduct = productRepository.save(item).block();

        assertNotNull(savedProduct.getId());

        Cart cart = new Cart();
        Cart savedCart = cartRepository.save(cart).block();

        CartItem cartItem = new CartItem(savedCart.getId(), savedProduct.getId(), 2);
        cartItemRepository.save(cartItem).block();

        Mono<Order> orderMono = orderService.createFromCart();

        StepVerifier.create(orderMono)
                .assertNext(order -> {
                    assertNotNull(order.getId());
                    assertEquals(100000L, order.getTotalSum());
                    StepVerifier.create(orderRepository.findById(order.getId()))
                            .expectNextCount(1)
                            .verifyComplete();
                    StepVerifier.create(orderItemRepository.findByOrderId(order.getId()))
                            .assertNext(items -> {
                                assertEquals(savedProduct.getId(), items.getItemId());
                                assertEquals(2, items.getCount());
                                assertEquals(order.getId(), items.getOrderId());
                            })
                            .expectComplete()
                            .verify();
                })
                .expectComplete()
                .verify();
        StepVerifier.create(cartItemRepository.findByCartId(savedCart.getId()))
                .expectComplete()
                .verify();
    }

}