package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HighlightServiceImplTest {
    private HighlightServiceImpl highlightService;

    @BeforeEach
    void setUp() {
        highlightService = new HighlightServiceImpl();
    }

    @Test
    void listHighlights_returnsEmptyList() {
        List<HighlightDto> result = highlightService.listHighlights("fixture1");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findHighlightById_returnsEmptyOptional() {
        Optional<HighlightDto> result = highlightService.findHighlightById("highlight1");
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void createHighlight_returnsMockDto() {
        CreateHighlightDto createDto = CreateHighlightDto.builder()
                .fixtureId("fixture1")
                .title("Great Goal")
                .videoUrl("http://example.com/video.mp4")
                .type("goal")
                .build();
        HighlightDto result = highlightService.createHighlight(createDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("fixture1", result.getFixtureId());
        assertEquals("Great Goal", result.getTitle());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateHighlight_returnsMockDto() {
        String highlightId = "highlightToUpdate";
        UpdateHighlightDto updateDto = UpdateHighlightDto.builder()
                .title("Even Greater Goal")
                .description("What a strike!")
                .build();
        HighlightDto result = highlightService.updateHighlight(highlightId, updateDto);
        assertNotNull(result);
        assertEquals(highlightId, result.getId());
        assertEquals("Even Greater Goal", result.getTitle());
        assertEquals("What a strike!", result.getDescription()); // Mock default if null in DTO
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateHighlight_usesOriginalValues_whenUpdateDtoFieldsAreNull() {
        String highlightId = "highlight-abc";
        UpdateHighlightDto updateDtoWithNulls = new UpdateHighlightDto(); // All fields null

        HighlightDto result = highlightService.updateHighlight(highlightId, updateDtoWithNulls);
        assertNotNull(result);
        assertEquals(highlightId, result.getId());
        assertEquals("Original Title", result.getTitle()); // From mock implementation
        assertEquals("Original Description", result.getDescription()); // From mock
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void deleteHighlight_completesWithoutError() {
        assertDoesNotThrow(() -> highlightService.deleteHighlight("highlightToDelete"));
    }
}
