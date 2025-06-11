package co.hublots.ln_foot.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils; // For checking if this class is used by service

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.Highlight;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.HighlightRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HighlightServiceImplTest {

    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private FixtureRepository fixtureRepository;

    private HighlightServiceImpl highlightService;

    @BeforeEach
    void setUp() {
        highlightService = new HighlightServiceImpl(highlightRepository, fixtureRepository);
    }

    private Fixture createMockFixture(String internalId, String apiFixtureId) {
        return Fixture.builder().id(internalId).apiFixtureId(apiFixtureId).status("FT").matchDatetime(LocalDateTime.now()).build();
    }

    private Highlight createMockHighlight(String id, Fixture fixture, String title) {
        return Highlight.builder()
                .id(id)
                .fixture(fixture)
                .title(title)
                .videoUrl("http://video.url/mock.mp4")
                .thumbnailUrl("http://thumb.url/mock.jpg")
                .source("TestSource")
                .duration(120)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void listHighlightsByFixture_whenFixtureApiIdProvided_returnsPagedDtos() { // Renamed test
        // Arrange
        String fixtureApiId = "FX_API_1";
        Pageable pageable = PageRequest.of(0, 10);
        Fixture mockFixture = createMockFixture(UUID.randomUUID().toString(), fixtureApiId); // Fixture mock still useful for context if needed by mapToDto indirectly
        Highlight mockHighlight = createMockHighlight(UUID.randomUUID().toString(), mockFixture, "Goal!");
        Page<Highlight> highlightPage = new PageImpl<>(List.of(mockHighlight), pageable, 1);

        when(highlightRepository.findByFixture_ApiFixtureId(fixtureApiId, pageable)).thenReturn(highlightPage);

        // Act
        Page<HighlightDto> result = highlightService.listHighlightsByFixture(fixtureApiId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockHighlight.getTitle(), result.getContent().get(0).getTitle());
        assertEquals(fixtureApiId, result.getContent().get(0).getFixtureId());
        verify(highlightRepository).findByFixture_ApiFixtureId(fixtureApiId, pageable);
    }

    @Test
    void listHighlightsByFixture_whenFixtureApiIdNullOrBlank_throwsIllegalArgumentException() { // Updated test
        Pageable pageable = PageRequest.of(0, 10);
        Exception eNull = assertThrows(IllegalArgumentException.class, () -> highlightService.listHighlightsByFixture(null, pageable));
        assertEquals("fixtureApiId cannot be null or empty when listing highlights.", eNull.getMessage());

        Exception eBlank = assertThrows(IllegalArgumentException.class, () -> highlightService.listHighlightsByFixture("  ", pageable));
        assertEquals("fixtureApiId cannot be null or empty when listing highlights.", eBlank.getMessage());

        verify(highlightRepository, never()).findByFixture_ApiFixtureId(anyString(), any(Pageable.class));
    }

    @Test
    void listHighlightsByFixture_whenNoHighlightsFound_returnsEmptyPage() { // New test
        String fixtureApiId = "FX_API_NO_HIGHLIGHTS";
        Pageable pageable = PageRequest.of(0,10);
        when(highlightRepository.findByFixture_ApiFixtureId(fixtureApiId, pageable)).thenReturn(Page.empty(pageable));

        Page<HighlightDto> result = highlightService.listHighlightsByFixture(fixtureApiId, pageable);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void findHighlightById_whenFound_returnsOptionalDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Fixture mockFixture = createMockFixture(UUID.randomUUID().toString(), "FX_API_HL");
        Highlight mockHighlight = createMockHighlight(id, mockFixture, "Amazing Save");
        when(highlightRepository.findById(id)).thenReturn(Optional.of(mockHighlight));

        // Act
        Optional<HighlightDto> result = highlightService.findHighlightById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockHighlight.getTitle(), result.get().getTitle());
        assertEquals(mockFixture.getApiFixtureId(), result.get().getFixtureId());
        verify(highlightRepository).findById(id);
    }

    @Test
    void createHighlight_savesAndReturnsDto() {
        // Arrange
        String fixtureApiId = "FX_FOR_NEW_HL";
        String internalFixtureId = UUID.randomUUID().toString();
        Fixture mockFixture = createMockFixture(internalFixtureId, fixtureApiId);

        CreateHighlightDto createDto = CreateHighlightDto.builder()
                .fixtureId(fixtureApiId)
                .title("New Highlight")
                .videoUrl("http://new.video/hl.mp4")
                .durationSeconds(90)
                .build();

        when(fixtureRepository.findByApiFixtureId(fixtureApiId)).thenReturn(Optional.of(mockFixture));

        ArgumentCaptor<Highlight> highlightCaptor = ArgumentCaptor.forClass(Highlight.class);
        when(highlightRepository.save(highlightCaptor.capture())).thenAnswer(invocation -> {
            Highlight savedHl = invocation.getArgument(0);
            savedHl.setId(UUID.randomUUID().toString());
            savedHl.setCreatedAt(LocalDateTime.now());
            savedHl.setUpdatedAt(LocalDateTime.now());
            return savedHl;
        });

        // Act
        HighlightDto resultDto = highlightService.createHighlight(createDto);

        // Assert
        assertNotNull(resultDto);
        assertNotNull(resultDto.getId());
        assertEquals(createDto.getTitle(), resultDto.getTitle());
        assertEquals(fixtureApiId, resultDto.getFixtureId());
        assertEquals(createDto.getDurationSeconds(), resultDto.getDurationSeconds());

        Highlight captured = highlightCaptor.getValue();
        assertEquals(mockFixture, captured.getFixture());
        assertEquals(createDto.getTitle(), captured.getTitle());

        verify(fixtureRepository).findByApiFixtureId(fixtureApiId);
        verify(highlightRepository).save(any(Highlight.class));
    }

    @Test
    void createHighlight_fixtureNotFound_throwsException() {
        CreateHighlightDto createDto = CreateHighlightDto.builder().fixtureId("UNKNOWN_FX").title("Test").build();
        when(fixtureRepository.findByApiFixtureId("UNKNOWN_FX")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> highlightService.createHighlight(createDto));
        verify(highlightRepository, never()).save(any(Highlight.class));
    }


    @Test
    void updateHighlight_whenFound_updatesAndReturnsDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Fixture mockFixture = createMockFixture(UUID.randomUUID().toString(), "FX_HL_UPD");
        Highlight existingHighlight = createMockHighlight(id, mockFixture, "Old Title");
        when(highlightRepository.findById(id)).thenReturn(Optional.of(existingHighlight));

        UpdateHighlightDto updateDto = UpdateHighlightDto.builder().title("Updated Title").durationSeconds(150).build();

        when(highlightRepository.save(any(Highlight.class))).thenAnswer(invocation -> {
            Highlight hlToSave = invocation.getArgument(0);
            hlToSave.setUpdatedAt(LocalDateTime.now());
            return hlToSave;
        });

        // Act
        HighlightDto resultDto = highlightService.updateHighlight(id, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(id, resultDto.getId());
        assertEquals("Updated Title", resultDto.getTitle());
        assertEquals(150, resultDto.getDurationSeconds());
        assertTrue(resultDto.getUpdatedAt().isAfter(existingHighlight.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1)));

        verify(highlightRepository).findById(id);
        ArgumentCaptor<Highlight> hlCaptor = ArgumentCaptor.forClass(Highlight.class);
        verify(highlightRepository).save(hlCaptor.capture());
        assertEquals("Updated Title", hlCaptor.getValue().getTitle());
    }

    @Test
    void deleteHighlight_whenFound_deletesHighlight() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(highlightRepository.existsById(id)).thenReturn(true);
        doNothing().when(highlightRepository).deleteById(id);

        // Act
        assertDoesNotThrow(() -> highlightService.deleteHighlight(id));

        // Assert
        verify(highlightRepository).existsById(id);
        verify(highlightRepository).deleteById(id);
    }
}
