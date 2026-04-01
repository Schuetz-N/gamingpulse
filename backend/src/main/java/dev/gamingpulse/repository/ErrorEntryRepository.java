package dev.gamingpulse.repository;

import dev.gamingpulse.model.ErrorEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorEntryRepository extends JpaRepository<ErrorEntry, Long> {

    List<ErrorEntry> findTop100ByOrderByOccurredAtDesc();

    @Query(value = """
            SELECT source, COUNT(*) AS count
            FROM error_entry
            WHERE occurred_at > :since
            GROUP BY source
            ORDER BY count DESC
            """, nativeQuery = true)
    List<Object[]> findErrorCountsBySourceSince(@Param("since") long since);
}