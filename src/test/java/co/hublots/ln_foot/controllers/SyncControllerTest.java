package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.*;
import co.hublots.ln_foot.services.SyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SyncService syncService;

    @Autowired
    private ObjectMapper objectMapper;

    private SyncResultDto createMockSyncResult(String operation, int count) {
        return SyncResultDto.builder()
                .status("success")
                .message("Successfully synced " + count + " items for " + operation)
                .count(count)
                .operationType(operation)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncLeagues_isOk_withAdminRole() throws Exception {
        SyncLeaguesDto syncDto = new SyncLeaguesDto(); // Can be empty or populated
        SyncResultDto resultDto = createMockSyncResult("syncLeagues", 10);
        when(syncService.syncLeagues(any(SyncLeaguesDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/sync/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.count", is(10)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncLeagues_isOk_withAdminRole_andEmptyBody() throws Exception {
        SyncResultDto resultDto = createMockSyncResult("syncLeaguesEmptyBody", 5); // Service mock handles null DTO
        when(syncService.syncLeagues(any(SyncLeaguesDto.class))).thenReturn(resultDto); // any() will match the new SyncLeaguesDto() in controller

        mockMvc.perform(post("/api/v1/sync/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Empty JSON object, or no content at all if allowed
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.count", is(5)));
    }


    @Test
    void syncLeagues_isUnauthorized_withoutAuth() throws Exception {
        SyncLeaguesDto syncDto = new SyncLeaguesDto();
        mockMvc.perform(post("/api/v1/sync/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void syncLeagues_isForbidden_withUserRole() throws Exception {
        SyncLeaguesDto syncDto = new SyncLeaguesDto();
        mockMvc.perform(post("/api/v1/sync/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncTeamsByLeague_isOk_withAdminRole() throws Exception {
        SyncTeamsByLeagueDto syncDto = SyncTeamsByLeagueDto.builder().leagueId("L1").season("2023").build();
        SyncResultDto resultDto = createMockSyncResult("syncTeamsByLeague", 20);
        when(syncService.syncTeamsByLeague(any(SyncTeamsByLeagueDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/sync/teams-by-league")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(20)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncFixturesByLeague_isOk_withAdminRole() throws Exception {
        SyncFixturesByLeagueDto syncDto = SyncFixturesByLeagueDto.builder().leagueId("L1").build();
        SyncResultDto resultDto = createMockSyncResult("syncFixturesByLeague", 50);
        when(syncService.syncFixturesByLeague(any(SyncFixturesByLeagueDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/sync/fixtures-by-league")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(50)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncFixtureDetails_isOk_withAdminRole() throws Exception {
        SyncFixtureDetailsDto syncDto = SyncFixtureDetailsDto.builder().fixtureId("F1").build();
        SyncResultDto resultDto = createMockSyncResult("syncFixtureDetails", 1);
        when(syncService.syncFixtureDetails(any(SyncFixtureDetailsDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/v1/sync/fixture-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)));
    }
}
