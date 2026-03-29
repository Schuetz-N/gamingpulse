package dev.gamingpulse.controller;

import dev.gamingpulse.model.Post;
import dev.gamingpulse.service.PostHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostHistoryService postHistoryService;

    public PostController(PostHistoryService postHistoryService) {
        this.postHistoryService = postHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getRecentPosts() {
        return ResponseEntity.ok(postHistoryService.getRecentPosts());
    }

    @PostMapping
    public ResponseEntity<Post> logPost(@RequestBody Map<String, String> body) {
        Post post = postHistoryService.savePost(
                body.getOrDefault("title", ""),
                body.getOrDefault("link", ""),
                body.getOrDefault("source", "unknown"),
                body.getOrDefault("category", "gaming"),
                body.getOrDefault("summary", ""),
                Boolean.parseBoolean(body.getOrDefault("success", "true"))
        );
        return ResponseEntity.ok(post);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "todayCount", postHistoryService.getTodayPostCount(),
                "bySource", postHistoryService.getPostCountBySource()
        ));
    }
}
