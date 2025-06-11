package co.hublots.ln_foot.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.models.Fixture;
import co.hublots.ln_foot.models.Highlight;
import co.hublots.ln_foot.repositories.FixtureRepository;
import co.hublots.ln_foot.repositories.HighlightRepository;
import co.hublots.ln_foot.services.HighlightService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Collections; // Added import
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;
    private final FixtureRepository fixtureRepository;

    private HighlightDto mapToDto(Highlight entity) {
        if (entity == null) {
            return null;
        }
        return HighlightDto.builder()
                .id(entity.getId())
                .fixtureId(entity.getFixture() != null ? entity.getFixture().getApiFixtureId() : null)
                .title(entity.getTitle())
                .description(entity.getDescription()) // Added mapping
                .videoUrl(entity.getVideoUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .durationSeconds(entity.getDuration())
                .type(entity.getType()) // Added mapping
                // source from entity not in DTO
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateHighlightDto dto, Highlight entity, Fixture fixture) {
        entity.setFixture(fixture);
        entity.setTitle(dto.getTitle());
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setThumbnailUrl(dto.getThumbnailUrl());
        entity.setDuration(dto.getDurationSeconds());
        entity.setDescription(dto.getDescription()); // Added mapping
        entity.setType(dto.getType());           // Added mapping
        // Entity's source could be set here if provided in DTO or from context, e.g. entity.setSource("internal_upload");
    }

    private void mapToEntityForUpdate(UpdateHighlightDto dto, Highlight entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getVideoUrl() != null) {
            entity.setVideoUrl(dto.getVideoUrl());
        }
        if (dto.getThumbnailUrl() != null) {
            entity.setThumbnailUrl(dto.getThumbnailUrl());
        }
        if (dto.getDurationSeconds() != null) {
            entity.setDuration(dto.getDurationSeconds());
        }
        if (dto.getDescription() != null) { // Added mapping
            entity.setDescription(dto.getDescription());
        }
        if (dto.getType() != null) { // Added mapping
            entity.setType(dto.getType());
        }
    }


    // Removed comment as imports are now at the top

    @Override
    @Transactional(readOnly = true)
    public Page<HighlightDto> listHighlightsByFixture(String fixtureApiId, Pageable pageable) { // Changed signature
        if (!StringUtils.hasText(fixtureApiId)) {
            throw new IllegalArgumentException("fixtureApiId cannot be null or empty when listing highlights.");
        }
        // The repository method findByFixture_ApiFixtureId directly uses the fixture's API ID.
        // No need to fetch Fixture entity first to get its internal ID if repo method handles it.
        Page<Highlight> highlightPage = highlightRepository.findByFixture_ApiFixtureId(fixtureApiId, pageable);
        return highlightPage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HighlightDto> findHighlightById(String id) {
        return highlightRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public HighlightDto createHighlight(CreateHighlightDto createDto) {
        Fixture fixture = fixtureRepository.findByApiFixtureId(createDto.getFixtureId())
                .orElseThrow(() -> new EntityNotFoundException("Fixture with apiFixtureId " + createDto.getFixtureId() + " not found"));

        Highlight highlight = new Highlight();
        mapToEntityForCreate(createDto, highlight, fixture);
        Highlight savedHighlight = highlightRepository.save(highlight);
        return mapToDto(savedHighlight);
    }

    @Override
    @Transactional
    public HighlightDto updateHighlight(String id, UpdateHighlightDto updateDto) {
        Highlight highlight = highlightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Highlight with ID " + id + " not found"));
        mapToEntityForUpdate(updateDto, highlight);
        Highlight updatedHighlight = highlightRepository.save(highlight);
        return mapToDto(updatedHighlight);
    }

    @Override
    @Transactional
    public void deleteHighlight(String id) {
        if (!highlightRepository.existsById(id)) {
            throw new EntityNotFoundException("Highlight with ID " + id + " not found");
        }
        highlightRepository.deleteById(id);
    }
}
