package ru.yandex.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.dto.Order;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;
import java.util.Optional;

public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String viewAllOrders(Model model) {
        List<OrderDto> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);

        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") boolean newOrder, Model model) throws IllegalArgumentException {
        Optional<OrderDto> order = orderService.findOrder(id);
        model.addAttribute("order", order.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")));

        return "order";
    }

    @PostMapping("/buy")
    public String createOrder(RedirectAttributes redirectAttributes) {
        Order order = orderService.createFromCart();
        Long orderId = order.getId();

        redirectAttributes.addAttribute("newOrder", "true");
        return "redirect:/orders/" + orderId;
    }
}