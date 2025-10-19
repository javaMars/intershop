package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import ru.yandex.practicum.mymarket.dto.Item;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<Item, Long> {
    @NonNull
    Page<Item> findAll(@NonNull Pageable pageable);

    Page<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Optional<Item> findById(Long id);

    void save(Item item);
}
