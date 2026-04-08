package com.example.gitleaks;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3DocumentStorageService {

    private final String accessKey;
    private final String secretKey;

    public S3DocumentStorageService(
            @Value("${amazon.accessKey}") String accessKey,
            @Value("${amazon.secretKey}") String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public Map<String, Object> simulateUpload() {
        String bucketName = "gitleaks-demo-artifacts";
        String objectKey = "reports/scan-" + Instant.now().toEpochMilli() + ".json";
        String payload = "{\"status\":\"ok\",\"source\":\"gitleaks-demo\"}";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("application/json")
                .build();

        RequestBody requestBody = RequestBody.fromString(payload, StandardCharsets.UTF_8);

        try (S3Client ignored = buildClient()) {
            return Map.of(
                    "provider", "aws-s3",
                    "bucket", bucketName,
                    "objectKey", objectKey,
                    "contentType", request.contentType(),
                    "payloadBytes", requestBody.optionalContentLength().orElse(0L),
                    "accessKeyPreview", mask(accessKey, 4),
                    "operation", "PutObject");
        }
    }

    private S3Client buildClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private String mask(String value, int visibleChars) {
        if (value == null || value.isBlank()) {
            return "missing";
        }

        int keep = Math.min(visibleChars, value.length());
        return value.substring(0, keep) + "*".repeat(Math.max(0, value.length() - keep));
    }
}
