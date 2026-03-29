package dev.gamingpulse.service;

import dev.gamingpulse.model.SeenUrl;
import dev.gamingpulse.repository.SeenUrlRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DeduplicationService {

    private final SeenUrlRepository seenUrlRepository;

    public DeduplicationService(SeenUrlRepository seenUrlRepository) {
        this.seenUrlRepository = seenUrlRepository;
    }

    public boolean hasBeenSeen(String url) {
        String normalized = normalizeUrl(url);
        return seenUrlRepository.existsByUrl(normalized);
    }

    @Transactional
    public boolean markAsSeen(String url, String source) {
        String normalized = normalizeUrl(url);
        if (seenUrlRepository.existsByUrl(normalized)) {
            return false;
        }
        seenUrlRepository.save(new SeenUrl(normalized, source));
        return true;
    }

    public long getSeenCount() {
        return seenUrlRepository.count();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldEntries() {
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        int deleted = seenUrlRepository.deleteOlderThan(sevenDaysAgo);
        if (deleted > 0) {
            System.out.println("Cleaned up " + deleted + " old seen URLs");
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return "";
        return url.split("\\?")[0]
                   .split("#")[0]
                   .replaceAll("/+$", "")
                   .toLowerCase();
    }
}
