package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncFixturesByLeagueDto {
    private String leagueId; // Internal or external league ID
    private String season; // Optional: filter by season
    private LocalDate dateFrom; // Optional: filter by date range
    private LocalDate dateTo; // Optional: filter by date range
}
