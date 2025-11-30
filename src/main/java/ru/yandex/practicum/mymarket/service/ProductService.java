package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

public interface ProductService {
    Flux<Item> findAll();
    Flux<Item> findByTitle(String trim);
    Mono<Item> findById(Long id);
}
