package dev.gamingpulse.service;

import dev.gamingpulse.model.Post;
import dev.gamingpulse.repository.PostRepository;
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
        return postRepository.findAll().stream()
                .filter(p -> p.getPostedAt().isAfter(weekAgo))
                .collect(Collectors.groupingBy(Post::getSource, Collectors.counting()));
    }
}
