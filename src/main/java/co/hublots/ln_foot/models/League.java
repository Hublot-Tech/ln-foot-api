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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_leagues")
public class League {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "league_name", nullable = false)
    private String leagueName;

    private String country; // Country where the league is based

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "sport_id") // From external API, e.g., ID for "Football"
    private String sportId;

    @Column(name = "api_league_id", unique = true) // External API ID
    private String apiLeagueId;

    @Column(name = "api_source") // e.g., "api-football"
    private String apiSource;

    private Integer tier; // e.g., 1, 2, 3 for league level

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Fixture> fixtures;

    // Highlights can also be directly associated with a league sometimes (e.g. weekly top 5 goals of the league)
    // @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // private List<Highlight> leagueHighlights;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
