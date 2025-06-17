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

// Import Fixture and necessary JPA annotations
import co.hublots.ln_foot.models.Fixture;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_highlights", schema = "lnfoot_api")
public class Highlight {

    @Id
    @UuidGenerator
    private String id;

    private String title; // Optional title for the highlight

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private String source; // e.g., "YouTube", "Vimeo", "Internal"

    private Integer duration; // Duration in seconds

    @Lob // Assuming description can be long
    private String description;

    private String type; // e.g., "goal", "foul", "card"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id") // Name of the foreign key column in web_highlights table
    private Fixture fixture;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
