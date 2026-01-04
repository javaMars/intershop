package ru.yandex.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.UserForm;
import ru.yandex.practicum.mymarket.service.UserService;

import java.util.Map;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public Mono<String> login(@RequestParam Map<String, String> params, Model model) {
        if (params.containsKey("error")) {
            model.addAttribute("showError", true);
        } else if (params.containsKey("logout")) {
            model.addAttribute("showLogout", true);
        } else if (params.containsKey("registered")) {
            model.addAttribute("showRegistered", true);
        }
        return Mono.just("login");
    }

    @GetMapping("/register")
    public Mono<String> registerForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return Mono.just("register");
    }

    @PostMapping("/register")
    public Mono<String> register(@ModelAttribute UserForm form, Model model) {
        if (form.getLogin() == null || form.getLogin().trim().length() < 3) {
            model.addAttribute("error", "Логин минимум 3 символа");
            return Mono.just("register");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            model.addAttribute("error", "Пароль минимум 6 символов");
            return Mono.just("register");
        }

        return userService.register(form)
                .then(Mono.just("redirect:/login?registered"))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    model.addAttribute("error", ex.getMessage());
                    return Mono.just("register");
                });
    }
}