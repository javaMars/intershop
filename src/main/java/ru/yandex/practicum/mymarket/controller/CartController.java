package ru.yandex.practicum.mymarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemForm;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<String> viewCart(Model model) {
        return cartService.findCartItemsWithDetails()
                .map(items -> {
                    model.addAttribute("items", items);
                    long total = items.stream()
                            .mapToLong(i -> i.getPrice() * i.getCount())
                            .sum();
                    model.addAttribute("total", total);
                    return "cart";
                });
    }

    @PostMapping
    public Mono<String> updateCartItem(
            @ModelAttribute ItemForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return Mono.just("cart/items/form");
        }
        Long itemId = form.getId();
        String action = form.getAction();

        return cartService.handleItemAction(itemId, action)
                .then(cartService.findCartItemsWithDetails())
                .map(items -> {
                    model.addAttribute("items", items);
                    long total = items.stream()
                            .mapToLong(i -> i.getPrice() * i.getCount())
                            .sum();
                    model.addAttribute("total", total);
                    return "cart";
                });
    }
}