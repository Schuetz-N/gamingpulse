package dev.gamingpulse.service;

import dev.gamingpulse.model.ServiceHealthRecord;
import dev.gamingpulse.repository.ServiceHealthRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class HealthCheckService {

    private final HttpClient httpClient;
    private final ServiceHealthRecordRepository healthRecordRepository;

    private volatile ServiceHealthRecord latestN8n;
    private volatile ServiceHealthRecord latestGroq;

    @Value("${gamingpulse.n8n-url:http://n8n:5678}")
    private String n8nUrl;

    @Value("${gamingpulse.groq-health-url:https://api.groq.com/openai/v1/models}")
    private String groqHealthUrl;

    @Value("${gamingpulse.groq-api-key:}")
    private String groqApiKey;

    public HealthCheckService(ServiceHealthRecordRepository healthRecordRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    @Transactional
    public void runChecks() {
        latestN8n  = persistCheck("n8n",  n8nUrl + "/healthz", null);
        latestGroq = persistCheck("groq", groqHealthUrl, groqApiKey);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldRecords() {
        healthRecordRepository.deleteOlderThan(Instant.now().minus(7, ChronoUnit.DAYS));
    }

    public Map<String, Object> getFullStatus() {
        return Map.of(
                "backend", Map.of("status", "up"),
                "n8n",     latestN8n  != null ? toStatusMap(latestN8n)  : Map.of("status", "starting"),
                "groq",    latestGroq != null ? toStatusMap(latestGroq) : Map.of("status", "starting")
        );
    }

    public List<ServiceHealthRecord> getHistory(Instant since) {
        return healthRecordRepository.findByCheckedAtAfterOrderByCheckedAtAsc(since);
    }

    private ServiceHealthRecord persistCheck(String service, String url, String bearerToken) {
        CheckResult result = checkService(url, bearerToken);
        ServiceHealthRecord record = new ServiceHealthRecord(service, result.status(), result.errorDetail());
        return healthRecordRepository.save(record);
    }

    private CheckResult checkService(String url, String bearerToken) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET();
            if (bearerToken != null && !bearerToken.isBlank()) {
                builder.header("Authorization", "Bearer " + bearerToken);
            }
            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new CheckResult("up", null);
            }
            return new CheckResult("degraded", "HTTP " + response.statusCode());
        } catch (Exception e) {
            return new CheckResult("down", e.getMessage() != null
                    ? e.getMessage()
                    : e.getClass().getSimpleName());
        }
    }

    private Map<String, Object> toStatusMap(ServiceHealthRecord r) {
        if (r.getErrorDetail() != null) {
            return Map.of("status", r.getStatus(), "error", r.getErrorDetail());
        }
        return Map.of("status", r.getStatus());
    }

    private record CheckResult(String status, String errorDetail) {}
}