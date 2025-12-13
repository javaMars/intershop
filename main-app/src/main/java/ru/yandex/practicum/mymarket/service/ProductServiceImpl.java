package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.ItemCache;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import org.springframework.data.domain.Pageable;
import java.time.Duration;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ReactiveRedisTemplate<String, ItemCache> itemRedisTemplate;
    private final Duration cacheDuration = Duration.ofMinutes(3);


    public ProductServiceImpl(ProductRepository productRepository,
                              @Qualifier("itemRedisTemplate") ReactiveRedisTemplate<String, ItemCache> itemRedisTemplate) {
        this.productRepository = productRepository;
        this.itemRedisTemplate = itemRedisTemplate;
    }

    public Flux<Item> findAll(Pageable pageable) {
        return productRepository.findIds(pageable) // возвращает Flux<String>
                .filter(id -> id != null)
                .flatMap(id -> getProductFromCacheOrDb(id));
    }

    private Mono<Item> getProductFromCacheOrDb(Long id) {
        String key = "product:" + id;

        return itemRedisTemplate.opsForValue().get(key)
                .flatMap(this::toDb)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(product -> saveToCache(key, product).thenReturn(product))
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

    public Mono<Item> toDb(ItemCache cacheItem) {
        if (cacheItem == null ||
                cacheItem.getId() == null || cacheItem.getId().trim().isEmpty() ||
                cacheItem.getPrice() == null || cacheItem.getPrice().trim().isEmpty()) {
            return Mono.empty();
        }

        try {
            Item dbItem = new Item();
            dbItem.setId(Long.parseLong(cacheItem.getId().trim()));
            dbItem.setTitle(cacheItem.getTitle() != null ? cacheItem.getTitle() : "");
            dbItem.setDescription(cacheItem.getDescription() != null ? cacheItem.getDescription() : "");
            dbItem.setImgPath(cacheItem.getImgPath() != null ? cacheItem.getImgPath() : "");
            dbItem.setPrice(Long.parseLong(cacheItem.getPrice().trim()));

            return Mono.just(dbItem);
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    private Mono<Void> saveToCache(String key, Item product) {
        return toCache(product)
                .flatMap(cacheItem ->
                        itemRedisTemplate.opsForValue()
                                .set(key, cacheItem, Duration.ofHours(1))
                ).then();
    }

    public Flux<Item> findByTitle(String title, Pageable pageable) {
        return productRepository.findByTitleContaining(title, pageable);
    }

    public Mono<Item> findById(Long id) {
        String key = "product:" + id;
        return itemRedisTemplate.opsForValue().get(key)
                .flatMap(this::toDb)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(dbItem -> {
                                    return saveToCache(key, dbItem)
                                            .thenReturn(dbItem);
                                })
                );
    }
}
