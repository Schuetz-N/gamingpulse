package dev.gamingpulse.controller;

import dev.gamingpulse.model.ServiceHealthRecord;
import dev.gamingpulse.service.DeduplicationService;
import dev.gamingpulse.service.HealthCheckService;
import dev.gamingpulse.service.PostHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
                        "postsToday",    postHistoryService.getTodayPostCount(),
                        "seenUrls",      deduplicationService.getSeenCount(),
                        "postsBySource", postHistoryService.getPostCountBySource()
                )
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(healthCheckService.getFullStatus());
    }

    @GetMapping("/history")
    public ResponseEntity<List<ServiceHealthRecord>> getHistory(
            @RequestParam(defaultValue = "60") int minutes) {
        int clamped = Math.max(5, Math.min(minutes, 1440));
        Instant since = Instant.now().minus(clamped, ChronoUnit.MINUTES);
        return ResponseEntity.ok(healthCheckService.getHistory(since));
    }
}