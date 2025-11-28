package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Page<Item> findAll(Pageable pageable){
        return productRepository.findAll(pageable);
    }

    public Page<Item> findByTitle(String title, Pageable pageable) {
        return productRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    public Optional<Item> findById(Long id) {
        return productRepository.findById(id);
    }
}
