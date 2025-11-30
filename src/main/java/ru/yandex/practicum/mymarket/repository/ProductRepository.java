package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Item, Long> {
    @NonNull
    Flux<Item> findAll();

    Flux<Item> findByTitleContainingIgnoreCase(String title);

    Mono<Item> findById(Long id);
}
