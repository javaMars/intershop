package ru.yandex.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.api.DefaultApi;
import ru.yandex.practicum.mymarket.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final DefaultApi paymentClient;

    public OrderController(OrderService orderService, DefaultApi paymentClient) {
        this.orderService = orderService;
        this.paymentClient = paymentClient;
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
        String userId = "user123";

        return orderService.createFromCart()
                .flatMap(orderDto -> {
                    PaymentRequest paymentRequest = new PaymentRequest()
                            .userId(userId)
                            .orderId("order-" + orderDto.getId())
                            .amount(orderDto.getTotalSum().doubleValue());

                    return paymentClient.apiPaymentsPayPost(paymentRequest)
                            .flatMap(response -> {
                                if (response.getSuccess()) {
                                    return Mono.just("redirect:/orders/" + orderDto.getId() + "?newOrder=true");
                                } else {
                                    return orderService.cancelOrder(orderDto.getId())
                                            .then(Mono.just("redirect:/cart?error=payment_failed"));
                                }
                            });
                })
                .onErrorResume(e -> Mono.just("redirect:/cart?error=payment_unavailable"));
    }
}