package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HighlightServiceImpl implements HighlightService {

    @Override
    public List<HighlightDto> listHighlights(String fixtureId) {
        // Mock: Filter by fixtureId if a list of highlights was present
        return Collections.emptyList();
    }

    @Override
    public Optional<HighlightDto> findHighlightById(String id) {
        return Optional.empty();
    }

    @Override
    public HighlightDto createHighlight(CreateHighlightDto createDto) {
        return HighlightDto.builder()
                .id(UUID.randomUUID().toString())
                .fixtureId(createDto.getFixtureId())
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .videoUrl(createDto.getVideoUrl())
                .thumbnailUrl(createDto.getThumbnailUrl())
                .durationSeconds(createDto.getDurationSeconds())
                .type(createDto.getType())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public HighlightDto updateHighlight(String id, UpdateHighlightDto updateDto) {
        // Assume fetch, then update
        return HighlightDto.builder()
                .id(id)
                .fixtureId("originalFixtureId") // Usually not updatable
                .title(updateDto.getTitle() != null ? updateDto.getTitle() : "Original Title")
                .description(updateDto.getDescription() != null ? updateDto.getDescription() : "Original Description")
                .videoUrl(updateDto.getVideoUrl() != null ? updateDto.getVideoUrl() : "http://original.video/url")
                .thumbnailUrl(updateDto.getThumbnailUrl() != null ? updateDto.getThumbnailUrl() : "http://original.thumb/url")
                .durationSeconds(updateDto.getDurationSeconds() != null ? updateDto.getDurationSeconds() : 60)
                .type(updateDto.getType() != null ? updateDto.getType() : "goal")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public void deleteHighlight(String id) {
        System.out.println("Deleting highlight with id: " + id);
    }
}
