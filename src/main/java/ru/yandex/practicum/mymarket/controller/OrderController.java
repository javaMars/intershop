package ru.yandex.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public Mono<String> viewAllOrders(Model model) {
        return orderService.findAllOrders()
                .collectList()
                        .map(orders -> {
                            model.addAttribute("orders", orders);
                            return "orders";
                        });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> viewOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") boolean newOrder, Model model) throws IllegalArgumentException {
        return orderService.findOrder(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")))
                .map(order -> {
                    model.addAttribute("order", order);
                    return "order";
                });
    }

    @PostMapping("/buy")
    public Mono<String> createOrder() {
        return orderService.createFromCart()
                            .flatMap(order -> Mono.just("redirect:/orders/" + order.getId() + "?newOrder=true"))
                .onErrorReturn("redirect:/cart");
    }
}