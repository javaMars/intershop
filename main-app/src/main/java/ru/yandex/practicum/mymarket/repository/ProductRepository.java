package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;

import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findAll();
    @Query("SELECT p.id FROM items p WHERE p.id IS NOT NULL ORDER BY p.id")
    Flux<Long> findIds(Pageable pageable);

    Flux<Item> findAllById(Iterable<Long> ids);

    @Query("SELECT * FROM items WHERE LOWER(title) LIKE LOWER('%' || :title || '%') ORDER BY id")
    Flux<Item> findByTitleContaining(@Param("title") String title, Pageable pageable);

    Mono<Item> findById(Long id);
}
