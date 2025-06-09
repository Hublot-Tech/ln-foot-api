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
@Table(name = "web_highlights")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false) // A highlight must belong to a fixture
    private Fixture fixture;

    // Optional: if highlights can be directly associated with a league independent of a fixture
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "league_id")
    // private League league;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
