package ru.yandex.practicum.mymarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(Model model) {
        List<CartItem> cartItems = cartService.findCartItems();

        long total = cartItems.stream()
                .mapToLong(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                .sum();

        model.addAttribute("items", cartItems);
        model.addAttribute("total", total);

        return "cart";
    }

    @PostMapping
    public String updateCartItem(
            @RequestParam("id") Long itemId,
            @RequestParam("action") String action,
            Model model
    ) {
        try {
            cartService.handleItemAction(itemId, action);
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при обработке действия");
        }

        List<CartItem> cartItems = cartService.findCartItems();
        long total = cartItems.stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getCount())
                .sum();

        model.addAttribute("items", cartItems);
        model.addAttribute("total", total);

        return "cart";
    }
}