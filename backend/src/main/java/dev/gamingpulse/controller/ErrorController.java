package dev.gamingpulse.controller;

import dev.gamingpulse.model.ErrorEntry;
import dev.gamingpulse.service.ErrorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errors")
public class ErrorController {

    private final ErrorService errorService;

    public ErrorController(ErrorService errorService) {
        this.errorService = errorService;
    }

    @GetMapping
    public ResponseEntity<List<ErrorEntry>> getRecentErrors() {
        return ResponseEntity.ok(errorService.getRecentErrors());
    }

    @PostMapping
    public ResponseEntity<ErrorEntry> logError(@RequestBody Map<String, String> body) {
        ErrorEntry entry = errorService.logError(
                body.getOrDefault("source", "unknown"),
                body.getOrDefault("message", ""),
                body.getOrDefault("url", null)
        );
        return ResponseEntity.ok(entry);
    }

    // neu
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getErrorStats() {
        return ResponseEntity.ok(errorService.getErrorCountsBySource());
    }
}