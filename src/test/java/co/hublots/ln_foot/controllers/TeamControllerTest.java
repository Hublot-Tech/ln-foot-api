package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.services.TeamService;

@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

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
        when(teamService.listTeams(Optional.of("L1"))).thenReturn(Collections.singletonList(mockTeam));

        mockMvc.perform(get("/api/v1/teams").param("leagueId", "L1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("T1")));
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
}
