package com.example.gitleaks;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BackendCredentialsSimulator {

    private final String user;
    private final String password;
    private final String token;
    private final String jwtToken;

    public BackendCredentialsSimulator(
            @Value("${backend.user}") String user,
            @Value("${backend.password}") String password,
            @Value("${backend.token}") String token,
            @Value("${backend.jtw.token}") String jwtToken) {
        this.user = user;
        this.password = password;
        this.token = token;
        this.jwtToken = jwtToken;
    }

    public Map<String, Object> simulateAuthentication() {
        return Map.of(
                "user", user,
                "passwordLoaded", !password.isBlank(),
                "tokenPreview", mask(token, 6),
                "jwtIssuer", extractJwtSubject(jwtToken),
                "authMode", "service-account");
    }

    private String extractJwtSubject(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return "missing";
        }

        String[] parts = tokenValue.split("\\.");
        return parts.length == 3 ? "jwt-present" : "invalid-jwt-format";
    }

    private String mask(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "missing";
        }

        int keep = Math.min(visibleChars, value.length());
        return value.substring(0, keep) + "*".repeat(Math.max(0, value.length() - keep));
    }
}
