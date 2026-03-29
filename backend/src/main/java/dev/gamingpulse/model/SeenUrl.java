package dev.gamingpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "seen_urls")
public class SeenUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 1000)
    private String url;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private Instant seenAt;

    public SeenUrl() {}

    public SeenUrl(String url, String source) {
        this.url = url;
        this.source = source;
        this.seenAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getSource() { return source; }
    public Instant getSeenAt() { return seenAt; }

    public void setId(Long id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setSource(String source) { this.source = source; }
    public void setSeenAt(Instant seenAt) { this.seenAt = seenAt; }
}
