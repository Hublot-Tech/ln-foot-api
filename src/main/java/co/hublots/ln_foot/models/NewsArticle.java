package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_news_articles")
public class NewsArticle {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content; // Can be HTML or Markdown

    @Column(name = "author_name") // Used if not linking directly to a User entity, or as a fallback
    private String authorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Link to the User entity who authored this
    private User author;

    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    @Column(name = "source_url", length = 2048) // URLs can be long
    private String sourceUrl;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    private String category; // e.g., "General", "Transfers", "Match Report"

    // status from NewsArticleDto e.g., "published", "draft"
    private String status;

    // tags from NewsArticleDto e.g. ["transfer", "injury"]
    // This would typically be a ManyToMany to a Tag entity, or a List<String> with @ElementCollection
    // For simplicity based on current DTO (List<String>), using @ElementCollection for now.
    // @ElementCollection(fetch = FetchType.LAZY)
    // @CollectionTable(name = "news_article_tags", joinColumns = @JoinColumn(name = "news_article_id"))
    // @Column(name = "tag")
    // private List<String> tags;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
