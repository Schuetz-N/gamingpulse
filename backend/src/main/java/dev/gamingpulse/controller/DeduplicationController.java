package dev.gamingpulse.controller;

import dev.gamingpulse.service.DeduplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dedup")
public class DeduplicationController {

    private final DeduplicationService deduplicationService;

    public DeduplicationController(DeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        String source = body.getOrDefault("source", "unknown");

        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "url is required"));
        }

        boolean isNew = deduplicationService.markAsSeen(url, source);
        return ResponseEntity.ok(Map.of(
                "url", url,
                "isNew", isNew
        ));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCount() {
        return ResponseEntity.ok(Map.of("seenUrls", deduplicationService.getSeenCount()));
    }
}
