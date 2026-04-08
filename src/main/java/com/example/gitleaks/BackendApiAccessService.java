package com.example.gitleaks;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BackendApiAccessService {

    private final RestClient restClient;
    private final String user;
    private final String password;
    private final String apiKey;
    private final String bearerToken;

    public BackendApiAccessService(
            RestClient.Builder restClientBuilder,
            @Value("${backend.user}") String user,
            @Value("${backend.password}") String password,
            @Value("${backend.token}") String apiKey,
            @Value("${backend.jtw.token}") String bearerToken) {
        this.restClient = restClientBuilder
                .baseUrl("https://partner-api.internal.example")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.user = user;
        this.password = password;
        this.apiKey = apiKey;
        this.bearerToken = bearerToken;
    }

    public Map<String, Object> simulateSecuredRequest() {
        String authorizationHeader = "Basic " + encodeBasicAuth(user, password);

        // Production code would execute:
        // restClient.get()
        //         .uri("/v1/documents")
        //         .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
        //         .header("X-API-Key", apiKey)
        //         .header("X-Access-Token", bearerToken)
        //         .retrieve()
        //         .body(String.class);
        return Map.of(
                "baseUrl", "https://partner-api.internal.example",
                "resource", "/v1/documents",
                "httpMethod", "GET",
                "basicAuthUser", user,
                "authorizationHeaderPreview", mask(authorizationHeader, 10),
                "apiKeyHeader", "X-API-Key",
                "apiKeyPreview", mask(apiKey, 6),
                "tokenHeader", "X-Access-Token",
                "tokenPreview", mask(bearerToken, 12),
                "clientReady", restClient != null);
    }

    private String encodeBasicAuth(String username, String rawPassword) {
        String credentials = username + ":" + rawPassword;
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String mask(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "missing";
        }

        int keep = Math.min(visibleChars, value.length());
        return value.substring(0, keep) + "*".repeat(Math.max(0, value.length() - keep));
    }
}
