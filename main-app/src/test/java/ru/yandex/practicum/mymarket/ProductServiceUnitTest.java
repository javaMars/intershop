package ru.yandex.practicum.mymarket;

import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ProductRepository;
import ru.yandex.practicum.mymarket.service.ProductServiceImpl;

import java.time.Duration;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @Mock private ProductRepository productRepository;
    @Mock private ReactiveStringRedisTemplate itemRedisTemplate;
    @Mock private ReactiveValueOperations<String, String> redisValueOps;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, itemRedisTemplate);
    }

    @Test
    void findById_shouldReturnFromRepository() {
        when(itemRedisTemplate.opsForValue()).thenReturn(redisValueOps);
        when(redisValueOps.get(anyString())).thenReturn(Mono.empty());
        when(redisValueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.empty());
        when(productRepository.findById(123L)).thenReturn(Mono.just(new Item(123L, "Test", "", "", 1L)));

        StepVerifier.create(productService.findById(123L)).expectNextCount(1).verifyComplete();
    }

    @Test
    void findAll_shouldCallRepository() {
        when(itemRedisTemplate.opsForValue()).thenReturn(redisValueOps);
        when(redisValueOps.get(any())).thenReturn(Mono.empty());
        when(redisValueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.empty());

        Pageable pageable = PageRequest.of(0, 10);
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        when(productRepository.findIds(limit, offset)).thenReturn(Flux.just(1L));
        when(productRepository.findById(1L)).thenReturn(Mono.just(new Item(1L, "Test", "", "", 1L)));

        StepVerifier.create(productService.findAll(pageable)).expectNextCount(1).verifyComplete();
    }

    @Test
    void findByTitle_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByTitleContaining("test", pageable)).thenReturn(Flux.just(new Item(1L, "Test", "", "", 1L)));

        StepVerifier.create(productService.findByTitle("test", pageable)).expectNextCount(1).verifyComplete();
    }
}