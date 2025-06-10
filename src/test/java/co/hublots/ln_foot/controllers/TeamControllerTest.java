package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; // Added import
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @Autowired
    private ObjectMapper objectMapper;

    private TeamDto createMockTeamDto(String id) {
        return TeamDto.builder()
                .id(id)
                .name("Mock Team")
                .country("Mockland")
                .logoUrl("http://example.com/logo.png")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser // Endpoint is public
    void listTeamsByLeague_isOk() throws Exception {
        TeamDto mockTeam = createMockTeamDto("T1");
        when(teamService.listTeamsByLeague(anyString(), any())).thenReturn(Collections.singletonList(mockTeam));

        mockMvc.perform(get("/api/v1/teams").param("leagueId", "L1").param("season", "2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("T1")));
    }

    @Test
    @WithAnonymousUser
    void listTeamsByLeague_requiresLeagueId() throws Exception {
        // This test assumes leagueId is a required parameter.
        // Spring by default would return 400 if a required @RequestParam is missing.
        mockMvc.perform(get("/api/v1/teams")) // No leagueId param
                .andExpect(status().isBadRequest()); // Or whatever error your setup produces for missing required params
    }


    @Test
    @WithAnonymousUser // Endpoint is public
    void findTeamById_isOk_whenFound() throws Exception {
        String teamId = "T123";
        TeamDto mockTeam = createMockTeamDto(teamId);
        when(teamService.findTeamById(teamId)).thenReturn(Optional.of(mockTeam));

        mockMvc.perform(get("/api/v1/teams/{id}", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(teamId)));
    }

    @Test
    @WithAnonymousUser // Endpoint is public
    void findTeamById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(teamService.findTeamById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/teams/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    // No Admin/Create/Update/Delete methods in TeamController based on prior analysis
}
