package co.hublots.ln_foot.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_teams", schema = "lnfoot_api")
public class Team {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    private String country;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "api_team_id", unique = true) // External API ID, should be unique per source
    private String apiTeamId;

    @Column(name = "api_source") // e.g., "api-football", "another-source"
    private String apiSource;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column(name = "stadium_name")
    private String stadiumName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
