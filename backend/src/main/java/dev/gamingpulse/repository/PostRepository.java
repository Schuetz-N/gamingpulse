package dev.gamingpulse.repository;

import dev.gamingpulse.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<Post> findAllByOrderByPostedAtDesc(Pageable pageable);

    @Query(value = """
            SELECT
                strftime(:pattern, datetime(posted_at / 1000, 'unixepoch')) AS bucket,
                COUNT(*) AS count
            FROM posts
            WHERE posted_at > :since
            GROUP BY bucket
            ORDER BY bucket ASC
            """, nativeQuery = true)
    List<Object[]> findPostCountsByBucket(
            @Param("pattern") String pattern,
            @Param("since")   long since
    );

    @Query(value = """
        SELECT source, COUNT(*) AS count
        FROM posts
        WHERE posted_at > :since
        GROUP BY source
        ORDER BY count DESC
        """, nativeQuery = true)
    List<Object[]> findSourceCountsSince(@Param("since") long since);
}