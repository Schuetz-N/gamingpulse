package dev.gamingpulse.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String link;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String category;

    @Column(length = 2000)
    private String summary;

    @Column(nullable = false)
    private Instant postedAt;

    @Column(nullable = false)
    private boolean success;

    public Post() {}

    public Post(String title, String link, String source, String category, String summary, boolean success) {
        this.title = title;
        this.link = link;
        this.source = source;
        this.category = category;
        this.summary = summary;
        this.success = success;
        this.postedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getSource() { return source; }
    public String getCategory() { return category; }
    public String getSummary() { return summary; }
    public Instant getPostedAt() { return postedAt; }
    public boolean isSuccess() { return success; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setLink(String link) { this.link = link; }
    public void setSource(String source) { this.source = source; }
    public void setCategory(String category) { this.category = category; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setPostedAt(Instant postedAt) { this.postedAt = postedAt; }
    public void setSuccess(boolean success) { this.success = success; }
}
