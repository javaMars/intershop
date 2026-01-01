package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.*;
import ru.yandex.practicum.mymarket.repository.*;
import ru.yandex.practicum.mymarket.service.OrderServiceImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceUnitTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderItemRepository, cartRepository, cartItemRepository, productRepository);
    }

    @Test
    void createFromCart_returnsMonoNotNull() {
        String userId = "user123";
        Cart cart = new Cart(); cart.setId(1L); cart.setUserId(userId);
        CartItem cartItem = new CartItem(1L, 10L, 2);
        Item product = new Item(10L, "Test", "", "", 100L);
        OrderItem orderItem = new OrderItem(); orderItem.setItemId(10L); orderItem.setCount(2);
        Order savedOrder = new Order(); savedOrder.setId(999L);

        when(cartRepository.findLastCartByUserId(userId)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(cartItem));
        when(productRepository.findById(10L)).thenReturn(Mono.just(product));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem));
        when(cartItemRepository.deleteByCartId(1L)).thenReturn(Mono.empty());

        when(productRepository.findAllById(anyList())).thenReturn(Flux.just(product));

        StepVerifier.create(orderService.createFromCart(userId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void createFromCart_withItems_returnsOrder() {
        String userId = "user123";

        Cart cart = new Cart(); cart.setId(1L); cart.setUserId(userId);
        CartItem cartItem = new CartItem(1L, 10L, 2);
        Item product = new Item(10L, "Test", "Desc", "img.jpg", 100L);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setItemId(10L);
        orderItem.setCount(2);
        Order savedOrder = new Order(); savedOrder.setId(999L);

        when(cartRepository.findLastCartByUserId(anyString())).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(anyLong())).thenReturn(Flux.just(cartItem));
        when(productRepository.findById(anyLong())).thenReturn(Mono.just(product));
        when(productRepository.findAllById(anyList())).thenReturn(Flux.just(product));
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.save(any())).thenReturn(Mono.just(orderItem));
        when(cartItemRepository.deleteByCartId(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createFromCart(userId))
                .expectNextCount(1)
                .verifyComplete();
    }
}