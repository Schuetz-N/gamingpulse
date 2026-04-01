package dev.gamingpulse.controller;

import dev.gamingpulse.model.Post;
import dev.gamingpulse.service.PostHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
                "bySource",   postHistoryService.getPostCountBySource()
        ));
    }

    @GetMapping
    public ResponseEntity<Page<Post>> getRecentPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        int clampedSize = Math.min(size, 100);
        PageRequest pageable = PageRequest.of(page, clampedSize, Sort.by("postedAt").descending());
        return ResponseEntity.ok(postHistoryService.getRecentPostsPaged(pageable));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PostHistoryService.PostBucketEntry>> getHistory(
            @RequestParam(defaultValue = "24h") String range) {
        return ResponseEntity.ok(postHistoryService.getPostHistory(range));
    }
}