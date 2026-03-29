package dev.gamingpulse.service;

import dev.gamingpulse.model.ErrorEntry;
import dev.gamingpulse.repository.ErrorEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
