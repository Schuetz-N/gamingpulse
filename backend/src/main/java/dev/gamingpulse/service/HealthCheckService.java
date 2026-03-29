package dev.gamingpulse.service;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${gamingpulse.ollama-url:http://ollama:11434}")
    private String ollamaUrl;

    @Value("${gamingpulse.n8n-url:http://n8n:5678}")
    private String n8nUrl;

    public HealthCheckService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, Object> getFullStatus() {
        return Map.of(
                "backend", Map.of("status", "up"),
                "ollama", checkService(ollamaUrl + "/api/tags"),
                "n8n", checkService(n8nUrl + "/healthz")
        );
    }

    private Map<String, Object> checkService(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return Map.of("status", "up", "code", response.statusCode());
            } else {
                return Map.of("status", "degraded", "code", response.statusCode());
            }
        } catch (Exception e) {
            return Map.of("status", "down", "error", e.getMessage());
        }
    }
}
