package co.hublots.ln_foot.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "highlights", schema = "lnfoot_web")
public class Highlight {

    @Id
    @UuidGenerator
    private String id;

    private String title; // Optional title for the highlight

    @Column(name = "video_url", nullable = false, length = 2048)
    private String videoUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    private String source; // e.g., "YouTube", "Vimeo", "Internal"

    @Column(name = "duration_seconds")
    @Min(1)
    private Integer durationSeconds; // Duration in second
    
    @Lob
    private String description;

    private String type; // e.g., "goal", "foul", "card"

    @ManyToOne(fetch = FetchType.LAZY)
    private Fixture fixture;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
