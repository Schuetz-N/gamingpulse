package dev.gamingpulse.service;

import dev.gamingpulse.model.ErrorEntry;
import dev.gamingpulse.repository.ErrorEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ErrorService {

    private final ErrorEntryRepository errorEntryRepository;

    public ErrorService(ErrorEntryRepository errorEntryRepository) {
        this.errorEntryRepository = errorEntryRepository;
    }

    public ErrorEntry logError(String source, String message, String url) {
        ErrorEntry entry = new ErrorEntry(source, message, url);
        return errorEntryRepository.save(entry);
    }

    public List<ErrorEntry> getRecentErrors() {
        return errorEntryRepository.findTop100ByOrderByOccurredAtDesc();
    }

    public Map<String, Long> getErrorCountsBySource() {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        return errorEntryRepository
                .findErrorCountsBySourceSince(since.toEpochMilli())
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }
}