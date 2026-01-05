package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.client.model.Balance;
import ru.yandex.practicum.mymarket.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.service.PaymentServiceClient;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentServiceClientIntegrationTest {

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @LocalServerPort
    private int port;

    @Test
    void initBalance_success() {
        Mono<Balance> result = paymentServiceClient.initBalance("user-123", 1000.0);

        StepVerifier.create(result)
                .expectNextMatches(balance ->
                        balance.getUserId().equals("user-123") &&
                                balance.getBalance() == 1000.0)
                .verifyComplete();
    }

    @Test
    void getBalance_notFound_returnsDefault() {
        Mono<Balance> result = paymentServiceClient.getBalance("unknown-user");

        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertEquals("unknown-user", balance.getUserId());
                    assertEquals(10000.0, balance.getBalance());
                })
                .verifyComplete();
    }
}