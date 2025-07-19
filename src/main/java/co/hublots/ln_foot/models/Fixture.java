package co.hublots.ln_foot.models;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fixtures", schema = "lnfoot_web", indexes = {
        @Index(name = "idx_fixture_api_id", columnList = "api_fixture_id"),
        @Index(name = "idx_fixture_datetime", columnList = "match_datetime")
})
public class Fixture {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "match_datetime", nullable = false)
    private OffsetDateTime matchDatetime;

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

    @Column(name = "venue_name")
    private String venueName;

    private Integer spectators;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "score_ht_home")
    private Integer scoreHtHome;
    
    @Column(name = "score_ht_away")
    private Integer scoreHtAway;
    
    @Column(name = "score_ft_home")
    private Integer scoreFtHome;
    
    @Column(name = "score_ft_away")
    private Integer scoreFtAway;

    @Column(name = "score_et_home")
    private Integer scoreEtHome;
    
    @Column(name = "score_et_away")
    private Integer scoreEtAway;
    
    @Column(name = "score_pt_home")
    private Integer scorePtHome;
    
    @Column(name = "score_pt_away")
    private Integer scorePtAway;

    @JsonIgnore
    @AssertTrue(message = "Home team (team1) and away team (team2) cannot be the same.")
    public boolean isTeam1AndTeam2Different() {
        if (this.team1 == null || this.team2 == null) {
            return true;
        }

        return !Objects.equals(this.team1.getId(), this.team2.getId());
    }
}
