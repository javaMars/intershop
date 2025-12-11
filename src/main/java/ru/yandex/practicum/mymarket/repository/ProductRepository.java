package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

@Repository
public interface ProductRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findAll();
    Flux<Item> findAllById(Iterable<Long> ids);

    Flux<Item> findByTitleContainingIgnoreCase(String title);

    Mono<Item> findById(Long id);
}
