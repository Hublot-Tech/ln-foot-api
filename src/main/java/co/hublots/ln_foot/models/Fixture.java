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
import java.util.List;
import java.util.Objects; // For Objects.equals

import jakarta.validation.constraints.AssertTrue; // Added import
import com.fasterxml.jackson.annotation.JsonIgnore; // Added import

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_fixtures")
public class Fixture {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "match_datetime", nullable = false)
    private LocalDateTime matchDatetime; // Using LocalDateTime assuming app consistency or UTC storage

    @Column(nullable = false)
    private String status; // e.g., "SCHEDULED", "LIVE", "FINISHED", "POSTPONED"

    private String round; // e.g., "Regular Season - 1", "Final"

    @Column(name = "api_fixture_id", unique = true)
    private String apiFixtureId;

    @Column(name = "api_source")
    private String apiSource;

    @Column(name = "goals_team1")
    private Integer goalsTeam1;

    @Column(name = "goals_team2")
    private Integer goalsTeam2;

    // Detailed scores (halftime, extratime, penalty) are available from external DTOs (e.g. ScoreDto)
    // but not persisted directly on the Fixture entity for now, only final scores (goalsTeam1, goalsTeam2).

    @Column(name = "venue_name")
    private String venueName;

    private Integer spectators;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false) // A fixture must belong to a league
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false) // Home team
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false) // Away team
    private Team team2;

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Highlight> highlights;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @AssertTrue(message = "Home team (team1) and away team (team2) cannot be the same.")
    @JsonIgnore // Avoid this appearing in JSON output if Fixture is ever directly serialized
    public boolean isTeam1AndTeam2Different() {
        // This validation is meaningful only if both teams are set.
        // The @JoinColumn for team1 and team2 are nullable = false, so they should always be present for a valid Fixture.
        if (this.team1 == null || this.team2 == null) {
            // This case should ideally not happen if team1 and team2 are non-nullable.
            // If they could be null for some reason (e.g. fixture planned but teams TBD),
            // then this validation should only apply when both are set.
            // Given nullable=false on JoinColumn, they should not be null.
            return true; // Or handle as an invalid state if this check is reached with nulls despite constraints.
                         // For AssertTrue, returning true means "passes validation" for this specific check.
                         // If one is null, they are "different" from the non-null one. If both null, "different" is moot.
        }
        // Compare by their primary IDs. Assumes Team entity has getId().
        return !Objects.equals(this.team1.getId(), this.team2.getId());
    }
}
