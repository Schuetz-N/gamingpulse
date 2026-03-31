package dev.gamingpulse.controller;

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

    @Value("${gamingpulse.groq-api-key:}")
    private String groqApiKey;

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
            String responseBody = response.body();

            int contentStart = responseBody.indexOf("\"content\":\"");
            if (contentStart == -1) {
                return ResponseEntity.ok(Map.of("summary", "No summary available"));
            }
            contentStart += 11;
            int contentEnd = responseBody.indexOf("\"", contentStart);
            while (contentEnd > 0 && responseBody.charAt(contentEnd - 1) == '\\') {
                contentEnd = responseBody.indexOf("\"", contentEnd + 1);
            }
            String summary = responseBody.substring(contentStart, contentEnd)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");

            return ResponseEntity.ok(Map.of("summary", summary));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("summary", "Summary generation failed: " + e.getMessage()));
        }
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
