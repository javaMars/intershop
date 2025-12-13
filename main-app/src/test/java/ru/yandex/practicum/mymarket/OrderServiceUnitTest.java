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
    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;


    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository, orderItemRepository,
                cartRepository, cartItemRepository, productRepository
        );
    }

    @Test
    void createFromCart_returnsMonoNotNull() {
        Cart cart = new Cart();
        cart.setId(1L);

        Item product = new Item();
        product.setId(10L);
        product.setPrice(50000L);

        CartItem cartItem = new CartItem(1L, 10L, 2);

        doReturn(Mono.just(cart)).when(cartRepository).findLastCart();
        doReturn(Flux.just(cartItem)).when(cartItemRepository).findByCartId(any());

        doReturn(Mono.just(product)).when(productRepository).findById(anyLong());
        doReturn(Mono.just(new Order())).when(orderRepository).save(any());
        doReturn(Mono.just(new OrderItem())).when(orderItemRepository).save(any());
        doReturn(Mono.empty()).when(cartItemRepository).deleteByCartId(any());

        StepVerifier.create(orderService.createFromCart())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void createFromCart_withItems_returnsOrder() {
        Cart cart = new Cart();
        cart.setId(1L);

        Item product = new Item();
        product.setId(10L);
        product.setPrice(50000L);

        CartItem cartItem = new CartItem(1L, 10L, 2);

        doReturn(Mono.just(cart)).when(cartRepository).findLastCart();
        doReturn(Flux.just(cartItem)).when(cartItemRepository).findByCartId(any());

        doReturn(Mono.just(product)).when(productRepository).findById((Long)any());
        doReturn(Mono.just(new Order())).when(orderRepository).save(any());
        doReturn(Mono.just(new OrderItem())).when(orderItemRepository).save(any());
        doReturn(Mono.empty()).when(cartItemRepository).deleteByCartId(any());

        StepVerifier.create(orderService.createFromCart())
                .expectNextCount(1)
                .verifyComplete();
    }
}