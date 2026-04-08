package com.example.gitleaks;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmazonCredentialsSimulator {

    private final String accessKey;
    private final String secretKey;

    public AmazonCredentialsSimulator(
            @Value("${amazon.accessKey}") String accessKey,
            @Value("${amazon.secretKey}") String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public Map<String, Object> simulateClientBootstrap() {
        return Map.of(
                "provider", "amazon",
                "accessKeyPreview", mask(accessKey, 4),
                "secretKeyLoaded", !secretKey.isBlank(),
                "signatureRegion", "us-east-1");
    }

    private String mask(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "missing";
        }

        int keep = Math.min(visibleChars, value.length());
        return value.substring(0, keep) + "*".repeat(Math.max(0, value.length() - keep));
    }
}
