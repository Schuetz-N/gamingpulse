package dev.gamingpulse.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/llm")
public class LlmController {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gamingpulse.groq-api-key:}")
    private String groqApiKey;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    public LlmController() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarize(@RequestBody Map<String, String> body) {
        String prompt = body.getOrDefault("prompt", "");
        if (prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "prompt is required"));
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String requestBody = """
                    {
                        "model": "llama-3.1-8b-instant",
                        "messages": [{"role": "user", "content": %s}],
                        "temperature": 0.3,
                        "max_tokens": 200
                    }
                    """.formatted(escapeJson(prompt));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 429) {
                    if (attempt < MAX_RETRIES) {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                        continue;
                    }
                    return ResponseEntity.ok(Map.of("summary", ""));
                }

                if (response.statusCode() != 200) {
                    return ResponseEntity.ok(Map.of("summary", ""));
                }

                JsonNode root = objectMapper.readTree(response.body());
                String summary = root
                        .path("choices")
                        .path(0)
                        .path("message")
                        .path("content")
                        .asText("");

                if (summary.isBlank()) {
                    return ResponseEntity.ok(Map.of("summary", ""));
                }

                return ResponseEntity.ok(Map.of("summary", summary.trim()));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.ok(Map.of("summary", ""));
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    return ResponseEntity.ok(Map.of("summary", ""));
                }
            }
        }

        return ResponseEntity.ok(Map.of("summary", ""));
    }

    private String escapeJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}