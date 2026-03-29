package dev.gamingpulse.repository;

import dev.gamingpulse.model.SeenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SeenUrlRepository extends JpaRepository<SeenUrl, Long> {

    boolean existsByUrl(String url);

    @Modifying
    @Query("DELETE FROM SeenUrl s WHERE s.seenAt < :before")
    int deleteOlderThan(Instant before);

    long count();
}
