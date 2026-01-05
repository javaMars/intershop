package ru.yandex.practicum.mymarket.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("scope")
    private String scope;

    public String getAccessToken() { return accessToken; }
    public int getExpiresIn() { return expiresIn; }
    public int getRefreshExpiresIn() { return refreshExpiresIn; }
    public String getTokenType() { return tokenType; }
    public String getScope() { return scope; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
    public void setRefreshExpiresIn(int refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setScope(String scope) { this.scope = scope; }
}

