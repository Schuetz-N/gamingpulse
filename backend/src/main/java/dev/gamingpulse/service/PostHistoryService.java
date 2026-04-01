package dev.gamingpulse.service;

import dev.gamingpulse.model.Post;
import dev.gamingpulse.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostHistoryService {

    private final PostRepository postRepository;

    public PostHistoryService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(String title, String link, String source, String category, String summary, boolean success) {
        Post post = new Post(title, link, source, category, summary, success);
        return postRepository.save(post);
    }

    public List<Post> getRecentPosts() {
        return postRepository.findTop50ByOrderByPostedAtDesc();
    }

    public long getTodayPostCount() {
        Instant todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS);
        return postRepository.countByPostedAtAfter(todayStart);
    }

    public Map<String, Long> getPostCountBySource() {
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Object[]> rows = postRepository.findSourceCountsSince(weekAgo.toEpochMilli());
        return rows.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
        ));
    }

    public Page<Post> getRecentPostsPaged(Pageable pageable) {
        return postRepository.findAllByOrderByPostedAtDesc(pageable);
    }

    public List<PostBucketEntry> getPostHistory(String range) {
        Instant since;
        String pattern;

        switch (range) {
            case "7d"  -> { since = Instant.now().minus(7,  ChronoUnit.DAYS);  pattern = "%Y-%m-%d"; }
            case "30d" -> { since = Instant.now().minus(30, ChronoUnit.DAYS);  pattern = "%Y-%m-%d"; }
            default    -> { since = Instant.now().minus(24, ChronoUnit.HOURS); pattern = "%Y-%m-%dT%H:00:00"; }
        }

        return postRepository
                .findPostCountsByBucket(pattern, since.toEpochMilli())
                .stream()
                .map(row -> new PostBucketEntry((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    public record PostBucketEntry(String bucket, long count) {}
}