package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.services.TeamService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {

    @Override
    public List<TeamDto> listTeamsByLeague(String leagueId, String season) {
        // Mock: filter by leagueId and season if data was present
        return Collections.emptyList();
    }

    @Override
    public Optional<TeamDto> findTeamById(String id) {
        // Mock implementation: Example of returning a specific mock DTO
        /*
        if ("mock-team-id".equals(id)) {
            return Optional.of(TeamDto.builder()
                    .id(id)
                    .name("Mock Team")
                    .country("Mockland")
                    .founded(1900)
                    .national(false)
                    .logoUrl("http://example.com/logo.png")
                    .venueName("Mock Stadium")
                    .venueAddress("123 Mock Street")
                    .venueCity("Mockville")
                    .venueCapacity(10000)
                    .createdAt(OffsetDateTime.now().minusDays(10))
                    .updatedAt(OffsetDateTime.now().minusDays(1))
                    .build());
        }
        */
        return Optional.empty();
    }
}
