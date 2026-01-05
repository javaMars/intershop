package ru.yandex.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.context.UserContext;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.PaymentServiceClient;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final PaymentServiceClient paymentServiceClient;

    public OrderController(OrderService orderService, PaymentServiceClient paymentServiceClient) {
        this.orderService = orderService;
        this.paymentServiceClient = paymentServiceClient;
    }

    @GetMapping("/orders")
    public Mono<String> viewAllOrders(Model model) {
        return UserContext.getCurrentUserId()
                .flatMap(userId ->
                        orderService.findOrdersByUserId(userId)
                                .collectList()
                                .map(orders -> {
                                    model.addAttribute("orders", orders);
                                    return "orders";
                                })
                );
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
        return UserContext.getCurrentUserId()
                .flatMap(userId ->
                        orderService.createFromCart(userId)
                                .flatMap(orderDto -> {
                                    PaymentRequest paymentRequest = new PaymentRequest()
                                            .userId(userId)
                                            .orderId(orderDto.getId().toString())
                                            .amount(orderDto.getTotalSum().doubleValue());

                                    return paymentServiceClient.pay(paymentRequest)
                                            .flatMap(response -> {
                                                if (response.getSuccess()) {
                                                    return orderService.clearCartAfterPayment(userId)
                                                            .then(Mono.just("redirect:/orders/" + orderDto.getId()));
                                                } else {
                                                    return orderService.cancelOrder(orderDto.getId())
                                                            .then(Mono.just("redirect:/cart/items?error=payment_failed"));
                                                }
                                            });
                                })
                )
                .onErrorResume(e -> {
                    return Mono.just("redirect:/cart/items?error=payment_unavailable");
                });
    }
}