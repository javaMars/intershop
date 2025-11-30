package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.*;

import java.util.List;
import java.util.Map;

@Service
@EnableTransactionManagement
public class CartServiceImpl implements CartService  {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final Mono<Cart> cart;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cart = cartRepository.save(new Cart());
    }

    public Flux<CartItem> findCartItems() {
        return cart.flatMapMany(cartFunc ->
                cartItemRepository.findByCartId(cartFunc.getId())
        );
    }

    public Mono<Integer> getItemCountInCart(Long itemId) {
        return cart.flatMap(cartObj ->
                productRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                        .flatMap(item ->
                                cartItemRepository.findByCartIdAndItemId(cartObj.getId(),item.getId())
                                        .map(CartItem::getCount)
                                        .defaultIfEmpty(0)
                        )
                );
    }

    public Mono<Map<Long, Integer>> getItemCountsMap() {
        return cart.flatMapMany(cartFunc -> cartItemRepository.findByCartId(cartFunc.getId())
                )
                .collectMap(
                        cartItem -> cartItem.getItem().getId(),
                        CartItem::getCount
                );

    }

    public Mono<Void> handleItemAction(Long itemId, String action) {
        return Mono.justOrEmpty(action)
                .map(String::toUpperCase)
                .filter(actionFilter -> List.of("MINUS", "PLUS", "DELETE").contains(actionFilter))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Некорректно указано действие: " + action)))
                .flatMap(normalizedAction -> {
                    if ("MINUS".equals(normalizedAction)) {
                        return decreaseItemCount(itemId);
                    } else if ("PLUS".equals(normalizedAction)) {
                        return increaseItemCount(itemId);
                    } else {
                        return removeItem(itemId);
                    }
                });
    }

    @Transactional
    Mono<Void> increaseItemCount(Long itemId) {
        return cart.flatMap(cartObj ->
                productRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                        .flatMap(item ->
                                cartItemRepository.findByCartIdAndItemId(cartObj.getId(), item.getId())
                                        .defaultIfEmpty(new CartItem(cartObj.getId(), item.getId(), 0))
                                        .flatMap(cartItem -> {
                                            cartItem.setCount(cartItem.getCount() + 1);
                                            return cartItemRepository.save(cartItem);
                                        })
                        )
        ).then();
    }


    @Transactional
    Mono<Void> decreaseItemCount(Long itemId) {
        return cart.flatMap(cartObj ->
                productRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                        .flatMap(item ->
                                cartItemRepository.findByCartIdAndItemId(cartObj.getId(), item.getId())
                                        .defaultIfEmpty(new CartItem(cartObj.getId(), item.getId(), 0))
                                        .flatMap(cartItem -> {
                                            int newCount = cartItem.getCount() - 1;
                                            if (newCount <= 0)
                                            {
                                                return cartItemRepository.delete(cartItem);
                                            } else {
                                                cartItem.setCount(newCount);
                                                return cartItemRepository.save(cartItem);
                                            }
                                        })
                        )
        ).then();
    }

    @Transactional
    Mono<Void> removeItem(Long itemId) {
        return cart.flatMap(cartObj ->
                productRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                        .flatMap((item ->
                                cartItemRepository.deleteByCartIdAndItemId(cartObj.getId(), itemId))
                        )
        ).then();
    }
}
