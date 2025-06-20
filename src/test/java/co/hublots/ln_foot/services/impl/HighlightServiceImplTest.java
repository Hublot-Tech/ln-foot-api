package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.models.Highlight;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.HighlightRepository;

@ExtendWith(MockitoExtension.class)
class HighlightServiceImplTest {

    @Mock
    private HighlightRepository highlightRepository;
    @Mock
    private FixtureRepository fixtureRepository;

    private HighlightServiceImpl highlightService;

    @BeforeEach
    void setUp() {
        highlightService = new HighlightServiceImpl(highlightRepository);
    }

    private Highlight createMockHighlight(String id, String title) {
        return Highlight.builder()
                .id(id)
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
    void listHighlights_returnsPagedDtos() { // Renamed test
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Highlight mockHighlight = createMockHighlight(UUID.randomUUID().toString(), "Goal!");
        Page<Highlight> highlightPage = new PageImpl<>(List.of(mockHighlight), pageable, 1);

        when(highlightRepository.findAll(pageable)).thenReturn(highlightPage);

        // Act
        Page<HighlightDto> result = highlightService.listHighlights(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockHighlight.getTitle(), result.getContent().get(0).getTitle());
        verify(highlightRepository).findAll(pageable);
    }

    @Test
    void findHighlightById_whenFound_returnsOptionalDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Highlight mockHighlight = createMockHighlight(id, "Amazing Save");
        when(highlightRepository.findById(id)).thenReturn(Optional.of(mockHighlight));

        // Act
        Optional<HighlightDto> result = highlightService.findHighlightById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockHighlight.getTitle(), result.get().getTitle());
        verify(highlightRepository).findById(id);
    }

    @Test
    void createHighlight_savesAndReturnsDto() {
        // Arrange
        CreateHighlightDto createDto = CreateHighlightDto.builder()
                .title("New Highlight")
                .videoUrl("http://new.video/hl.mp4")
                .durationSeconds(90)
                .build();


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
        assertEquals(createDto.getDurationSeconds(), resultDto.getDurationSeconds());

        Highlight captured = highlightCaptor.getValue();
        assertEquals(createDto.getTitle(), captured.getTitle());

        verify(highlightRepository).save(any(Highlight.class));
    }

    @Test
    void updateHighlight_whenFound_updatesAndReturnsDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Highlight existingHighlight = createMockHighlight(id, "Old Title");
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
        assertTrue(resultDto.getUpdatedAt()
                .isAfter(existingHighlight.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1)));

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
