package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SyncServiceImplTest {
    private SyncServiceImpl syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncServiceImpl();
    }

    @Test
    void syncLeagues_returnsSuccessDto() {
        SyncLeaguesDto syncDto = SyncLeaguesDto.builder()
                .externalLeagueIds(Collections.singletonList("leagueExt1"))
                .fullResync(false)
                .build();
        SyncResultDto result = syncService.syncLeagues(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("Successfully synced 1 leagues."));
        assertEquals(1, result.getCount());
        assertEquals("syncLeagues", result.getOperationType());
    }

    @Test
    void syncLeagues_fullResync_returnsSuccessDtoWithFullResyncMessage() {
        SyncLeaguesDto syncDto = SyncLeaguesDto.builder()
                .externalLeagueIds(Collections.singletonList("leagueExt1"))
                .fullResync(true)
                .build();
        SyncResultDto result = syncService.syncLeagues(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("Successfully performed full resync of 1 leagues."));
        assertEquals(1, result.getCount());
        assertEquals("syncLeaguesFullResync", result.getOperationType());
    }

    @Test
    void syncLeagues_noExternalIds_returnsDefaultCountMessage() {
        SyncLeaguesDto syncDto = SyncLeaguesDto.builder().fullResync(false).build(); // externalLeagueIds is null
        SyncResultDto result = syncService.syncLeagues(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("Successfully synced 5 leagues.")); // 5 is default in mock
        assertEquals(5, result.getCount());
    }


    @Test
    void syncTeamsByLeague_returnsSuccessDto() {
        SyncTeamsByLeagueDto syncDto = SyncTeamsByLeagueDto.builder()
                .leagueId("league1")
                .season("2023")
                .build();
        SyncResultDto result = syncService.syncTeamsByLeague(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("league league1 for season 2023"));
        assertEquals(15, result.getCount()); // Mock count
        assertEquals("syncTeamsByLeague", result.getOperationType());
    }

    @Test
    void syncFixturesByLeague_returnsSuccessDto() {
        SyncFixturesByLeagueDto syncDto = SyncFixturesByLeagueDto.builder()
                .leagueId("league1")
                .season("2023")
                .dateFrom(LocalDate.now().minusDays(7))
                .dateTo(LocalDate.now())
                .build();
        SyncResultDto result = syncService.syncFixturesByLeague(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("league league1 for season 2023 from"));
        assertEquals(25, result.getCount()); // Mock count
        assertEquals("syncFixturesByLeague", result.getOperationType());
    }

    @Test
    void syncFixturesByLeague_noOptionalParams_returnsSuccessDto() {
        SyncFixturesByLeagueDto syncDto = SyncFixturesByLeagueDto.builder()
                .leagueId("leagueOnly")
                .build();
        SyncResultDto result = syncService.syncFixturesByLeague(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("league leagueOnly"));
        assertFalse(result.getMessage().contains("for season")); // Check optional parts not included
        assertFalse(result.getMessage().contains("from"));
        assertEquals(25, result.getCount());
        assertEquals("syncFixturesByLeague", result.getOperationType());
    }

    @Test
    void syncFixtureDetails_returnsSuccessDto() {
        SyncFixtureDetailsDto syncDto = SyncFixtureDetailsDto.builder()
                .fixtureId("fixture123")
                .forceUpdate(false)
                .build();
        SyncResultDto result = syncService.syncFixtureDetails(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("fixture fixture123"));
        assertFalse(result.getMessage().contains("(Forced update)"));
        assertEquals(1, result.getCount());
        assertEquals("syncFixtureDetails", result.getOperationType());
    }

    @Test
    void syncFixtureDetails_forceUpdateTrue_returnsSuccessDtoWithForceMessage() {
        SyncFixtureDetailsDto syncDto = SyncFixtureDetailsDto.builder()
                .fixtureId("fixtureABC")
                .forceUpdate(true)
                .build();
        SyncResultDto result = syncService.syncFixtureDetails(syncDto);
        assertNotNull(result);
        assertEquals("success", result.getStatus());
        assertTrue(result.getMessage().contains("fixture fixtureABC"));
        assertTrue(result.getMessage().contains("(Forced update)"));
        assertEquals(1, result.getCount());
        assertEquals("syncFixtureDetails", result.getOperationType());
    }
}
