import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.PaymentServiceApplication;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.repository.BalanceRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = PaymentServiceApplication.class)
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldReturn401_WhenNoTokenProvided() {
        webClient.post().uri("/api/payments/pay")
                .bodyValue(new PaymentRequest().userId("test").amount(1000.0))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(authorities = "SERVICE")
    void processPayment_sufficientFunds_returnsSuccess() {
        webClient.post().uri("/api/payments/pay")
                .bodyValue(new PaymentRequest().userId("test-user-123").amount(1000.0))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> assertTrue(response.getSuccess()));
    }

    @Test
    @WithMockUser(authorities = "SERVICE")
    void processPayment_insufficientFunds_returnsFailure() {
        webClient.post().uri("/api/payments/pay")
                .bodyValue(new PaymentRequest().userId("test-user-123").amount(15000.0))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(PaymentResponse.class)
                .value(response -> assertFalse(response.getSuccess()));
    }

    @MockitoBean
    private BalanceRepository balanceRepository;

    @BeforeEach
    void setUp() {
        Balance updatedBalance = new Balance("test-user-123", 9000.0);
        when(balanceRepository.atomicDecrement("test-user-123", 1000.0))
                .thenReturn(Mono.just(updatedBalance));
        when(balanceRepository.atomicDecrement("test-user-123", 15000.0))
                .thenReturn(Mono.empty());
    }
}