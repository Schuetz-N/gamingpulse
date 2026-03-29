package dev.gamingpulse.repository;

import dev.gamingpulse.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByOrderByPostedAtDesc();

    List<Post> findByCategory(String category);

    List<Post> findBySource(String source);

    long countByPostedAtAfter(Instant since);

    long countBySourceAndPostedAtAfter(String source, Instant since);

    boolean existsByLink(String link);
}
