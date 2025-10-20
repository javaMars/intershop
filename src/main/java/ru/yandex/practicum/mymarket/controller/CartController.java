package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.dto.Item;
import ru.yandex.practicum.mymarket.service.ProductServiceImpl;

import java.util.List;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final ProductServiceImpl productService;

    public CartController(ProductServiceImpl productService) {
        this.productService = productService;
    }

    @GetMapping
    public String viewCart(Model model) {
        List<Item> cartItems = productService.getCartItems();

        long total = cartItems.stream()
                .mapToLong(item -> item.getPrice() * item.getCount())
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
        productService.handleItemAction(itemId, action);

        List<Item> cartItems = productService.getCartItems();
        model.addAttribute("cartItems", cartItems);

        return "cart";
    }
}