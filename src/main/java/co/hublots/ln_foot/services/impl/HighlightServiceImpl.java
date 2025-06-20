package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.models.Highlight;
import co.hublots.ln_foot.repositories.HighlightRepository;
import co.hublots.ln_foot.services.HighlightService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;

    private HighlightDto mapToDto(Highlight entity) {
        if (entity == null) {
            return null;
        }
        return HighlightDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .videoUrl(entity.getVideoUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .durationSeconds(entity.getDurationSeconds())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateHighlightDto dto, Highlight entity) {
        entity.setTitle(dto.getTitle());
        entity.setVideoUrl(dto.getVideoUrl());
        entity.setThumbnailUrl(dto.getThumbnailUrl());
        entity.setDurationSeconds(dto.getDurationSeconds());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
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
            entity.setDurationSeconds(dto.getDurationSeconds());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HighlightDto> listHighlights(Pageable pageable) {
        Page<Highlight> highlightPage = highlightRepository.findAll(pageable);
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
        Highlight highlight = new Highlight();
        mapToEntityForCreate(createDto, highlight);
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
