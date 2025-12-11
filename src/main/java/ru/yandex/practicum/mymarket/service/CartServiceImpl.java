package ru.yandex.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final Mono<Cart> cart;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cart = cartRepository.findLastCart();
    }

    public Mono<Cart> findLastCart() {
        return cartRepository.findLastCart()
                .switchIfEmpty(Mono.just(new Cart()));
    }

    public Flux<CartItem> findCartItems() {
        return findLastCart()
                .flatMapMany(cart -> {
                    Long cartId = cart.getId();
                    if (cartId == null) {
                        return Flux.empty();
                    }
                    return cartItemRepository.findByCartId(cartId);
                });
    }

    public Mono<Integer> getItemCountInCart(Long itemId) {
        return findLastCart()
                .flatMap(cartObj ->
                        productRepository.findById(itemId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                                .flatMap(item ->
                                        cartItemRepository.findByCartIdAndItemId(cartObj.getId(), item.getId())
                                                .map(CartItem::getCount)
                                                .defaultIfEmpty(0)
                                )
                );
    }

    public Mono<List<ItemDto>> findCartItemsWithDetails() {
        return findLastCart()
                .flatMapMany(cart -> {
                    Long cartId = cart.getId();
                    if (cartId == null) {
                        return Flux.empty();
                    }
                    return cartItemRepository.findByCartId(cartId);
                })
                .collectList()
                .flatMap(cartItems -> {
                    List<Long> itemIds = cartItems.stream()
                            .map(CartItem::getItemId)
                            .collect(Collectors.toList());

                    return productRepository.findAllById(itemIds)
                            .collectList()
                            .map(items -> {
                                Map<Long, Item> itemMap = items.stream()
                                        .collect(Collectors.toMap(Item::getId, Function.identity()));

                                return cartItems.stream()
                                        .map(ci -> {
                                            Item item = itemMap.get(ci.getItemId());
                                            return new ItemDto(item.getId(), item.getTitle(), item.getDescription(),
                                                    item.getImgPath(), item.getPrice(), ci.getCount());
                                        })
                                        .collect(Collectors.toList());
                            });
                });
    }

    public Mono<Map<Long, Integer>> getItemCountsMap() {
        return findLastCart()
                .flatMapMany(cart -> {
                    Long cartId = cart.getId();
                    if (cartId == null) {
                        return Flux.empty();
                    }
                    return cartItemRepository.findByCartId(cartId);
                })
                .filter(cartItem -> cartItem.getItemId() != null)
                .collectMap(CartItem::getItemId, CartItem::getCount);
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

    private Mono<Cart> getCurrentCart() {
        return
                cartRepository.findLastCart()
                        .switchIfEmpty(
                                Mono.defer(() -> {
                                    Cart newCart = new Cart();
                                    return cartRepository.save(newCart);
                                })
                        );
    }

    Mono<Void> increaseItemCount(Long itemId) {
        return getCurrentCart()
                .flatMap(cartObj -> {
                    Long cartId = cartObj.getId();

                    return productRepository.findById(itemId)
                            .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                            .flatMap(item ->
                                    cartItemRepository.findByCartIdAndItemId(cartId, item.getId())
                                            .switchIfEmpty(Mono.fromCallable(() -> createNewCartItem(cartId, item.getId())))
                                            .flatMap(cartItem -> {
                                                cartItem.setCount(cartItem.getCount() + 1);
                                                return cartItemRepository.save(cartItem);
                                            })
                            );
                })
                .then();
    }

    private CartItem createNewCartItem(Long cartId, Long itemId) {
        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setItemId(itemId);
        newItem.setCount(0);
        return newItem;
    }

    Mono<Void> decreaseItemCount(Long itemId) {
        return cart.flatMap(cartObj ->
                productRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден")))
                        .flatMap(item ->
                                cartItemRepository.findByCartIdAndItemId(cartObj.getId(), item.getId())
                                        .defaultIfEmpty(new CartItem(cartObj.getId(), item.getId(), 0))
                                        .flatMap(cartItem -> {
                                            int newCount = cartItem.getCount() - 1;
                                            if (newCount <= 0) {
                                                return cartItemRepository.delete(cartItem);
                                            } else {
                                                cartItem.setCount(newCount);
                                                return cartItemRepository.save(cartItem);
                                            }
                                        })
                        )
        ).then();
    }

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