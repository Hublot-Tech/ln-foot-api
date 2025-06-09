package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LeagueServiceImplTest {
    private LeagueServiceImpl leagueService;

    @BeforeEach
    void setUp() {
        leagueService = new LeagueServiceImpl();
    }

    @Test
    void listLeagues_returnsEmptyList() {
        List<LeagueDto> result = leagueService.listLeagues("England", "2023", "League");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findLeagueById_returnsEmptyOptional() {
        Optional<LeagueDto> result = leagueService.findLeagueById("league1");
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void createLeague_returnsMockDto() {
        CreateLeagueDto createDto = CreateLeagueDto.builder()
                .id("EPL") // Expecting external ID
                .name("English Premier League")
                .country("England")
                .season("2023")
                .type("League")
                .logoUrl("http://example.com/epl.png")
                .build();
        LeagueDto result = leagueService.createLeague(createDto);
        assertNotNull(result);
        assertEquals("EPL", result.getId());
        assertEquals("English Premier League", result.getName());
        assertTrue(result.getFixtures().isEmpty()); // Mock should return empty list
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateLeague_returnsMockDto() {
        String leagueId = "leagueToUpdate";
        UpdateLeagueDto updateDto = UpdateLeagueDto.builder()
                .name("Updated League Name")
                .logoUrl("http://example.com/new_logo.png")
                .build();
        LeagueDto result = leagueService.updateLeague(leagueId, updateDto);
        assertNotNull(result);
        assertEquals(leagueId, result.getId());
        assertEquals("Updated League Name", result.getName());
        assertEquals("http://example.com/new_logo.png", result.getLogoUrl());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateLeague_usesOriginalValues_whenUpdateDtoFieldsAreNull() {
        String leagueId = "league-xyz";
        UpdateLeagueDto updateDtoWithNulls = new UpdateLeagueDto();

        LeagueDto result = leagueService.updateLeague(leagueId, updateDtoWithNulls);
        assertNotNull(result);
        assertEquals(leagueId, result.getId());
        assertEquals("Original League Name", result.getName()); // From mock
        assertEquals("http://original.logo/url", result.getLogoUrl()); // From mock
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void deleteLeague_completesWithoutError() {
        assertDoesNotThrow(() -> leagueService.deleteLeague("leagueToDelete"));
    }
}
