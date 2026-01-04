package ru.yandex.practicum.mymarket.controller;
import ru.yandex.practicum.mymarket.client.api.DefaultApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.context.UserContext;
import ru.yandex.practicum.mymarket.dto.ItemForm;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.Collections;

@Controller
@RequestMapping("/cart/items")
public class CartController{

    private final CartService cartService;
    private final DefaultApi paymentClient;

    @Autowired
    public CartController(CartService cartService, DefaultApi paymentClient) {
        this.cartService = cartService;
        this.paymentClient = paymentClient;
    }

    @GetMapping
    public Mono<String> viewCart(Model model,
                                 @RequestParam(required = false) String error) {

        boolean hasPaymentError = "payment_unavailable".equals(error) || "payment_failed".equals(error);
        model.addAttribute("paymentError", hasPaymentError);

        return UserContext.getCurrentUserId()
                .flatMap(user ->
                        cartService.findCartItemsWithDetails(user)
                                .map(items -> {
                                    model.addAttribute("items", items);
                                    long total = items.stream()
                                            .mapToLong(i -> i.getPrice() * i.getCount())
                                            .sum();
                                    model.addAttribute("total", total);
                                    return total;
                                })
                                .flatMap(total ->
                                        paymentClient.apiPaymentsGetBalanceUserIdGet(user)
                                                .map(balance -> {
                                                    model.addAttribute("balance", balance.getBalance());
                                                    model.addAttribute("canPay", balance.getBalance() >= total);
                                                    return "cart";
                                                })
                                )
                )
                .onErrorResume(e -> {
                    model.addAttribute("items", Collections.emptyList());
                    model.addAttribute("total", 0L);
                    model.addAttribute("balance", 0.0);
                    model.addAttribute("canPay", false);
                    return Mono.just("cart");
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

        return UserContext.getCurrentUserId()
                .flatMap(userId ->
                        cartService.handleItemAction(itemId, action)
                                .then(cartService.findCartItemsWithDetails(userId))
                                .map(items -> {
                                    model.addAttribute("items", items);
                                    long total = items.stream()
                                            .mapToLong(i -> i.getPrice() * i.getCount())
                                            .sum();
                                    model.addAttribute("total", total);
                                    return total;
                                })
                                .flatMap(total -> paymentClient.apiPaymentsGetBalanceUserIdGet(userId)
                                        .map(balance -> {
                                            model.addAttribute("balance", balance.getBalance());
                                            model.addAttribute("canPay", balance.getBalance() >= total);
                                            model.addAttribute("paymentError", false);
                                            return "cart";
                                        })
                                )
                )
                .onErrorResume(e -> {
                    model.addAttribute("paymentError", true);
                    model.addAttribute("canPay", false);
                    model.addAttribute("balance", 0L);
                    return Mono.just("cart");
                });
    }
}