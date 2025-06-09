package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.models.Advertisement;
import co.hublots.ln_foot.repositories.AdvertisementRepository;
import co.hublots.ln_foot.services.AdvertisementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    private AdvertisementDto mapToDto(Advertisement entity) {
        if (entity == null) {
            return null;
        }
        return AdvertisementDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getDescription()) // Map description to content
                .url(entity.getReferenceUrl())   // Map referenceUrl to url
                .imageUrl(entity.getImageUrl())
                // startDate, endDate, priority, status are not in the current Advertisement entity
                // If they were, they would be mapped here:
                // .startDate(entity.getStartDate() != null ? entity.getStartDate().atOffset(ZoneOffset.UTC) : null)
                // .endDate(entity.getEndDate() != null ? entity.getEndDate().atOffset(ZoneOffset.UTC) : null)
                // .priority(entity.getPriority())
                // .status(entity.getStatus())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateAdvertisementDto dto, Advertisement entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getContent()); // Map content to description
        entity.setReferenceUrl(dto.getUrl());   // Map url to referenceUrl
        entity.setImageUrl(dto.getImageUrl());
        // startDate, endDate, priority, status are not in the current Advertisement entity
        // If they were, they would be mapped here:
        // entity.setStartDate(dto.getStartDate() != null ? dto.getStartDate().toLocalDateTime() : null);
        // entity.setEndDate(dto.getEndDate() != null ? dto.getEndDate().toLocalDateTime() : null);
        // entity.setPriority(dto.getPriority());
        // entity.setStatus(dto.getStatus());
    }

    private void mapToEntityForUpdate(UpdateAdvertisementDto dto, Advertisement entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            entity.setDescription(dto.getContent()); // Map content to description
        }
        if (dto.getUrl() != null) {
            entity.setReferenceUrl(dto.getUrl());   // Map url to referenceUrl
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
        // startDate, endDate, priority, status are not in the current Advertisement entity
        // if (dto.getStartDate() != null) {
        //     entity.setStartDate(dto.getStartDate().toLocalDateTime());
        // }
        // if (dto.getEndDate() != null) {
        //     entity.setEndDate(dto.getEndDate().toLocalDateTime());
        // }
        // if (dto.getPriority() != null) {
        //     entity.setPriority(dto.getPriority());
        // }
        // if (dto.getStatus() != null) {
        //     entity.setStatus(dto.getStatus());
        // }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvertisementDto> getLatestAdvertisements() {
        // Assuming we want top 10 latest based on createdAt, which is available in Advertisement entity
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        return advertisementRepository.findAll(pageRequest).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdvertisementDto> getAdvertisementById(String id) {
        return advertisementRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public AdvertisementDto createAdvertisement(CreateAdvertisementDto createDto) {
        Advertisement advertisement = new Advertisement();
        mapToEntityForCreate(createDto, advertisement);
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        return mapToDto(savedAdvertisement);
    }

    @Override
    @Transactional
    public AdvertisementDto updateAdvertisement(String id, UpdateAdvertisementDto updateDto) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement with ID " + id + " not found"));
        mapToEntityForUpdate(updateDto, advertisement);
        Advertisement updatedAdvertisement = advertisementRepository.save(advertisement);
        return mapToDto(updatedAdvertisement);
    }

    @Override
    @Transactional
    public void deleteAdvertisement(String id) {
        if (!advertisementRepository.existsById(id)) {
            throw new EntityNotFoundException("Advertisement with ID " + id + " not found");
        }
        advertisementRepository.deleteById(id);
    }
}
