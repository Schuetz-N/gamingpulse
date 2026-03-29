package dev.gamingpulse.controller;

import dev.gamingpulse.service.DeduplicationService;
import dev.gamingpulse.service.HealthCheckService;
import dev.gamingpulse.service.PostHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final HealthCheckService healthCheckService;
    private final PostHistoryService postHistoryService;
    private final DeduplicationService deduplicationService;

    public StatusController(HealthCheckService healthCheckService,
                            PostHistoryService postHistoryService,
                            DeduplicationService deduplicationService) {
        this.healthCheckService = healthCheckService;
        this.postHistoryService = postHistoryService;
        this.deduplicationService = deduplicationService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "services", healthCheckService.getFullStatus(),
                "stats", Map.of(
                        "postsToday", postHistoryService.getTodayPostCount(),
                        "seenUrls", deduplicationService.getSeenCount(),
                        "postsBySource", postHistoryService.getPostCountBySource()
                )
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(healthCheckService.getFullStatus());
    }
}
