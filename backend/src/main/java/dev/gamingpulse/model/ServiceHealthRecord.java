package dev.gamingpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "service_health_records", indexes = {
        @Index(name = "idx_shr_service_checked", columnList = "service, checkedAt"),
        @Index(name = "idx_shr_checked_at", columnList = "checkedAt")
})
public class ServiceHealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String service;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = true, length = 255)
    private String errorDetail;

    @Column(nullable = false)
    private Instant checkedAt;

    protected ServiceHealthRecord() {}

    public ServiceHealthRecord(String service, String status, String errorDetail) {
        this.service = service;
        this.status = status;
        this.errorDetail = errorDetail;
        this.checkedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getService() { return service; }
    public String getStatus() { return status; }
    public String getErrorDetail() { return errorDetail; }
    public Instant getCheckedAt() { return checkedAt; }
}