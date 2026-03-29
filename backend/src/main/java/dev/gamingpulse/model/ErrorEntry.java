package dev.gamingpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "errors")
public class ErrorEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, length = 2000)
    private String message;

    private String url;

    @Column(nullable = false)
    private Instant occurredAt;

    public ErrorEntry() {}

    public ErrorEntry(String source, String message, String url) {
        this.source = source;
        this.message = message;
        this.url = url;
        this.occurredAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public String getMessage() { return message; }
    public String getUrl() { return url; }
    public Instant getOccurredAt() { return occurredAt; }

    public void setId(Long id) { this.id = id; }
    public void setSource(String source) { this.source = source; }
    public void setMessage(String message) { this.message = message; }
    public void setUrl(String url) { this.url = url; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
