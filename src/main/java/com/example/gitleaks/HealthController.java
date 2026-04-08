package com.example.gitleaks;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final AmazonCredentialsSimulator amazonCredentialsSimulator;
    private final BackendCredentialsSimulator backendCredentialsSimulator;
    private final S3DocumentStorageService s3DocumentStorageService;
    private final BackendApiAccessService backendApiAccessService;

    public HealthController(
            AmazonCredentialsSimulator amazonCredentialsSimulator,
            BackendCredentialsSimulator backendCredentialsSimulator,
            S3DocumentStorageService s3DocumentStorageService,
            BackendApiAccessService backendApiAccessService) {
        this.amazonCredentialsSimulator = amazonCredentialsSimulator;
        this.backendCredentialsSimulator = backendCredentialsSimulator;
        this.s3DocumentStorageService = s3DocumentStorageService;
        this.backendApiAccessService = backendApiAccessService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "service", "gitleaks-backend");
    }

    @GetMapping("/integration-check")
    public Map<String, Object> integrationCheck() {
        return Map.of(
                "status", "ok",
                "service", "gitleaks-backend",
                "amazon", amazonCredentialsSimulator.simulateClientBootstrap(),
                "backend", backendCredentialsSimulator.simulateAuthentication(),
                "s3UploadFlow", s3DocumentStorageService.simulateUpload(),
                "backendHttpAccess", backendApiAccessService.simulateSecuredRequest());
    }
}
