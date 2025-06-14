package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.SimpleTeamDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.services.FixtureService;

@SpringBootTest
@AutoConfigureMockMvc
class FixtureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private FixtureService fixtureService;

    @Autowired
    private ObjectMapper objectMapper;

    private FixtureDto createMockFixtureDto(String id) {
        return FixtureDto.builder()
                .id(id)
                .leagueId("league1")
                .season("2023")
                .homeTeam(SimpleTeamDto.builder().id("teamA").name("Team A").build())
                .awayTeam(SimpleTeamDto.builder().id("teamB").name("Team B").build())
                .date(OffsetDateTime.now().plusDays(1))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser
    void listFixtures_isOk() throws Exception {
        FixtureDto mockFixture = createMockFixtureDto("fix1");
        when(fixtureService.listFixtures(any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockFixture)));

        mockMvc.perform(get("/api/v1/fixtures").param("leagueId", "L1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("fix1")));
    }

    @Test
    @WithAnonymousUser
    void findFixtureById_isOk_whenFound() throws Exception {
        String fixtureId = "fix123";
        FixtureDto mockFixture = createMockFixtureDto(fixtureId);
        when(fixtureService.findFixtureById(fixtureId)).thenReturn(Optional.of(mockFixture));

        mockMvc.perform(get("/api/v1/fixtures/{id}", fixtureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fixtureId)));
    }

    @Test
    @WithAnonymousUser
    void findFixtureById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(fixtureService.findFixtureById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/fixtures/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void getUpcomingFixtures_isOk() throws Exception {
        FixtureDto mockFixture = createMockFixtureDto("fixUpcoming");
        when(fixtureService.getUpcomingFixtures(anyInt(), any())).thenReturn(Collections.singletonList(mockFixture));

        mockMvc.perform(get("/api/v1/fixtures/upcoming").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("fixUpcoming")));
    }

    @Test
    @WithAnonymousUser
    void getFixturesByDate_isOk() throws Exception {
        FixtureDto mockFixture = createMockFixtureDto("fixByDate");
        LocalDate date = LocalDate.now();
        when(fixtureService.getFixturesByDate(eq(date), any())).thenReturn(Collections.singletonList(mockFixture));

        mockMvc.perform(get("/api/v1/fixtures/by-date").param("date", date.format(DateTimeFormatter.ISO_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("fixByDate")));
    }

    // --- Admin Endpoint Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void createFixture_isCreated_withAdminRole() throws Exception {
        CreateFixtureDto createDto = CreateFixtureDto.builder().leagueId("L1").homeTeamId("T1").awayTeamId("T2")
                .build();
        FixtureDto returnedDto = createMockFixtureDto(UUID.randomUUID().toString());
        when(fixtureService.createFixture(any(CreateFixtureDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/fixtures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(returnedDto.getId())));
    }

    @Test
    void createFixture_isUnauthorized_withoutAuth() throws Exception {
        CreateFixtureDto createDto = CreateFixtureDto.builder().build();
        mockMvc.perform(post("/api/v1/fixtures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createFixture_isForbidden_withUserRole() throws Exception {
        CreateFixtureDto createDto = CreateFixtureDto.builder().build();
        mockMvc.perform(post("/api/v1/fixtures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFixture_isOk_withAdminRole() throws Exception {
        String fixtureId = "fixtureToUpdate";
        UpdateFixtureDto updateDto = UpdateFixtureDto.builder().referee("New Ref").build();
        FixtureDto returnedDto = createMockFixtureDto(fixtureId);
        returnedDto.setReferee("New Ref");

        when(fixtureService.updateFixture(eq(fixtureId), any(UpdateFixtureDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/fixtures/{id}", fixtureId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referee", is("New Ref")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFixture_isNoContent_withAdminRole() throws Exception {
        String fixtureId = "fixtureToDelete";
        doNothing().when(fixtureService).deleteFixture(fixtureId);

        mockMvc.perform(delete("/api/v1/fixtures/{id}", fixtureId))
                .andExpect(status().isNoContent());
        verify(fixtureService, times(1)).deleteFixture(fixtureId);
    }
}
