package ru.yandex.practicum.mymarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mymarket.client.ApiClient;
import ru.yandex.practicum.mymarket.client.api.DefaultApi;

@Configuration
public class PaymentClientConfig {
    @Bean
    public ApiClient apiClient(WebClient.Builder webClientBuilder) {
        ApiClient apiClient = new ApiClient(webClientBuilder.build());
        apiClient.setBasePath("http://localhost:8082");
        return apiClient;
    }

    @Bean
    public DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}
