package dev.gamingpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class HealthCheckService {

    private final HttpClient httpClient;
    private volatile Map<String, Object> n8nStatus = Map.of("status", "starting");

    @Value("${gamingpulse.n8n-url:http://n8n:5678}")
    private String n8nUrl;

    public HealthCheckService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void updateN8nStatus() {
        n8nStatus = checkService(n8nUrl + "/rest/settings");
    }

    public Map<String, Object> getFullStatus() {
        return Map.of(
                "backend", Map.of("status", "up"),
                "n8n", n8nStatus
        );
    }

    private Map<String, Object> checkService(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Map.of("status", "up");
            } else {
                return Map.of("status", "degraded", "code", response.statusCode());
            }
        } catch (Exception e) {
            return Map.of("status", "down", "error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}