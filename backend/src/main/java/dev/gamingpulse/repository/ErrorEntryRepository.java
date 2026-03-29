package dev.gamingpulse.repository;

import dev.gamingpulse.model.ErrorEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorEntryRepository extends JpaRepository<ErrorEntry, Long> {

    List<ErrorEntry> findTop100ByOrderByOccurredAtDesc();
}
