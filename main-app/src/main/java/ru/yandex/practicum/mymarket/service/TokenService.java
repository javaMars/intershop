package ru.yandex.practicum.mymarket.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.TokenResponse;

@Service
public class TokenService {

    private final WebClient keycloakWebClient;
    private final String realm;
    private final String clientId;
    private final String clientSecret;

    public TokenService(@Qualifier("keycloakWebClient") WebClient keycloakWebClient,
                        @Value("${mymarket.keycloak.realm}") String realm,
                        @Value("${mymarket.keycloak.client-id}") String clientId,
                        @Value("${mymarket.keycloak.client-secret}") String clientSecret) {
        this.keycloakWebClient = keycloakWebClient;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Mono<String> getPaymentServiceToken() {
        String uri = "/realms/" + realm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        return keycloakWebClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getAccessToken);
    }
}