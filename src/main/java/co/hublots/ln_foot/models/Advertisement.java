package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime; // Corrected from java.time.OffsetDateTime for @CreationTimestamp

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_advertisements")
public class Advertisement {

    @Id
    @UuidGenerator
    private String id;

    @Column(nullable = false)
    private String title;

    @Lob // Assuming description can be long
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "reference_url")
    private String referenceUrl;

    // Fields from AdvertisementDto that might be relevant for an entity
    // These were not in the direct instructions for Advertisement.java but present in AdvertisementDto
    // For now, sticking to the explicitly requested fields for Advertisement.java
    // private LocalDateTime startDate; // Or OffsetDateTime if timezone is critical
    // private LocalDateTime endDate;
    // private Integer priority;
    // private String status;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
