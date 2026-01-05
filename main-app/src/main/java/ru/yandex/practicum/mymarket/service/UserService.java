package ru.yandex.practicum.mymarket.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.UserForm;
import ru.yandex.practicum.mymarket.model.Role;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final PaymentServiceClient paymentServiceClient;

    public UserService(UserRepository userRepository,
                       PasswordEncoder encoder,
                       PaymentServiceClient paymentServiceClient) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.paymentServiceClient = paymentServiceClient;
    }

    public Mono<User> register(UserForm form) {
        return userRepository.existsByLogin(form.getLogin())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Логин занят"));
                    }
                    User user = new User(form.getLogin(), encoder.encode(form.getPassword()), Role.USER);
                    return userRepository.save(user)
                            .flatMap(savedUser ->
                                    paymentServiceClient.initBalance(savedUser.getLogin(), 10000.0)
                                            .thenReturn(savedUser)
                            );
                });
    }
}