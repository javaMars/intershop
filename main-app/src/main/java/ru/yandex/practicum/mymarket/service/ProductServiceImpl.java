package ru.yandex.practicum.mymarket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.ItemCache;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import org.springframework.data.domain.Pageable;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ReactiveStringRedisTemplate itemRedisTemplate;
    private final Duration cacheDuration = Duration.ofMinutes(3);
    public final ObjectMapper objectMapper = new ObjectMapper();


    public ProductServiceImpl(ProductRepository productRepository,
                              @Qualifier("itemRedisTemplate") ReactiveStringRedisTemplate itemRedisTemplate) {
        this.productRepository = productRepository;
        this.itemRedisTemplate = itemRedisTemplate;
    }

    @Override
    public Flux<Item> findAll(Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        return productRepository.findIds(limit, offset)
                .collectList()
                .flatMapMany(this::getProductsBatchFromCacheOrDb);
    }
    private Flux<Item> getProductsBatchFromCacheOrDb(List<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(this::getProductFromCacheOrDb, 20)
                .distinct(Item::getId);
    }
    private Mono<Item> getProductFromCacheOrDb(Long id) {
        String key = "product:" + id;

        return itemRedisTemplate.opsForValue().get(key)
                .flatMap(this::toDb)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(product ->
                                        saveToCache(key, product).thenReturn(product)
                                )
                );
    }

    @Override
    public Mono<Item> findById(Long id) {
        String key = "product:" + id;
        return itemRedisTemplate.opsForValue().get(key)
                .flatMap(this::toDb)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(item -> saveToCache(key, item).thenReturn(item))
                );
    }

    public Mono<ItemCache> toCache(Item dbItem) {

        ItemCache cacheItem = new ItemCache();
        cacheItem.setId(String.valueOf(dbItem.getId()));
        cacheItem.setTitle(dbItem.getTitle());
        cacheItem.setDescription(dbItem.getDescription());
        cacheItem.setImgPath(dbItem.getImgPath());
        cacheItem.setPrice(String.valueOf(dbItem.getPrice()));

        return Mono.just(cacheItem);
    }

    public Mono<Item> toDb(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return Mono.empty();
        }

        try {
            ItemCache cacheItem = objectMapper.readValue(jsonString, ItemCache.class);
            Item dbItem = new Item();
            dbItem.setId(Long.parseLong(cacheItem.getId().trim()));
            dbItem.setTitle(Objects.toString(cacheItem.getTitle(), ""));
            dbItem.setDescription(Objects.toString(cacheItem.getDescription(), ""));
            dbItem.setImgPath(Objects.toString(cacheItem.getImgPath(), ""));
            dbItem.setPrice(Long.parseLong(cacheItem.getPrice().trim()));
            return Mono.just(dbItem);
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private Mono<Void> saveToCache(String key, Item product) {
        return toCache(product)
                .map(cacheItem -> {
                    try {
                        return objectMapper.writeValueAsString(cacheItem);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Ошибка в процессе сериализации кеша", e);
                    }
                })
                .flatMap(jsonString ->
                        itemRedisTemplate.opsForValue().set(key, jsonString, cacheDuration)
                )
                .then();
    }

    public Flux<Item> findByTitle(String title, Pageable pageable) {
        return productRepository.findByTitleContaining(title, pageable);
    }
}