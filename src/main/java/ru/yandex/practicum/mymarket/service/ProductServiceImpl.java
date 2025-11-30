package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    public Flux<Item> findAll(Pageable pageable){
        return productRepository.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
    }

    public Flux<Item> findByTitle(String title, Pageable pageable) {
        return productRepository.findByTitleContainingIgnoreCase(title)
                .skip(pageable.getOffset())
                .take(pageable.getPageSize());
    }

    public Mono<Item> findById(Long id) {
        return productRepository.findById(id);
    }
}
