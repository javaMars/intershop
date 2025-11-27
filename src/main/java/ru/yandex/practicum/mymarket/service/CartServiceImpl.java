package ru.yandex.practicum.mymarket.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService  {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private Cart cart;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cart = cartRepository.save(new Cart());
    }

    public List<CartItem> findCartItems() {
        return cartItemRepository.findByCart(cart);
    }

    public int getItemCountInCart(Long itemId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findByCartAndItem(cart, productRepository.findById(itemId).orElseThrow());
        return cartItemOpt.map(CartItem::getCount).orElse(0);
    }

    public Map<Long, Integer> getItemCountsMap() {
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        return cartItems.stream()
                .collect(Collectors.toMap(
                        ci -> ci.getItem().getId(),
                        CartItem::getCount
                ));
    }

    public void handleItemAction(Long itemId, String action) {
        String normalizedAction = Optional.ofNullable(action)
                .map(String::toUpperCase)
                .filter(actionFilter -> List.of("MINUS", "PLUS", "DELETE").contains(actionFilter))
                .orElseThrow(() -> new IllegalArgumentException("Некорректно указано действие: " + action));

        if ("MINUS".equals(normalizedAction)) {
            decreaseItemCount(itemId);
        } else if ("PLUS".equals(normalizedAction)){
            increaseItemCount(itemId);
        } else {
            removeItem(itemId);
        }
    }

    @Transactional
    void increaseItemCount(Long itemId) {
        Item item = productRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Товар не найден"));
        CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item).orElse(null);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setCount(1);
        } else {
            cartItem.setCount(cartItem.getCount() + 1);
        }
        cartItemRepository.save(cartItem);
    }

    @Transactional
    void decreaseItemCount(Long itemId) {
        Item item = productRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Товар не найден"));
        CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item).orElseThrow(() -> new RuntimeException("Товар в корзине не найден"));
        int newCount = cartItem.getCount() - 1;
        if (newCount <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setCount(newCount);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    void removeItem(Long itemId) {
        Item item = productRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Товар не найден"));
        System.out.println("ИД товара: " + item.getId());
        System.out.println("ИД корзины: " + cart.getId());
        cartItemRepository.deleteByCartAndItem(cart, item);
    }
}
