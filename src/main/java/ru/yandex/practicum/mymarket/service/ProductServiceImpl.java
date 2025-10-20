package ru.yandex.practicum.mymarket.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.dto.Item;
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

    public void handleItemAction(Long itemId, String action) {
        String normalizedAction = Optional.ofNullable(action)
                .map(String::toUpperCase)
                .filter(actionFilter -> List.of("MINUS", "PLUS").contains(actionFilter))
                .orElseThrow(() -> new IllegalArgumentException("Некорректно указано действие: " + action));

        if ("MINUS".equals(normalizedAction)) {
            decreaseItemCount(itemId);
        } else if ("PLUS".equals(normalizedAction)){
            increaseItemCount(itemId);
        } else {
            resetItemCount(itemId);
        }
    }

    public void increaseItemCount(Long id) {
        productRepository.findById(id).ifPresent(item -> {
            item.setCount(item.getCount() + 1);
            productRepository.save(item);
        });
    }

    public void decreaseItemCount(Long id) {
        productRepository.findById(id).ifPresent(item -> {
            if (item.getCount() > 0) {
                item.setCount(item.getCount() - 1);
                productRepository.save(item);
            }
        });
    }

    public void resetItemCount(Long id) {
        productRepository.findById(id).ifPresent(item -> {
            if (item.getCount() > 0) {
                item.setCount(0);
                productRepository.save(item);
            }
        });
    }

    public List<Item> getCartItems() {
        return productRepository.findItemsInCart().stream()
                .filter(item -> item.getCount() > 0) // товары в корзине
                .collect(Collectors.toList());
    }
}
