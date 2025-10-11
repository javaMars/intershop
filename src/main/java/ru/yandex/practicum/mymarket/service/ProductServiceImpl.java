package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import java.util.Optional;

public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Page<Item> findAll(Pageable pageable){
        return productRepository.findAll(pageable);
    }

    public Page<Item> findByTitle(String title, Pageable pageable) {
        return productRepository.findByTitle(title, pageable);
    }

    public Optional<Item> findById(Long id) {
        return productRepository.findById(id);
    }
}
