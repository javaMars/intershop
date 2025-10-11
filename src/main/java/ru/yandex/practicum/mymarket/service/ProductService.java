package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.mymarket.dto.Item;

import java.util.Optional;

public interface ProductService {
    Page<Item> findAll(Pageable pageable);
    Page<Item> findByTitle(String trim, Pageable pageable);
    Optional<Item> findById(Long id);
}
