package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.models.Advertisement;
import co.hublots.ln_foot.repositories.AdvertisementRepository;
import co.hublots.ln_foot.services.AdvertisementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                .content(entity.getDescription())
                .url(entity.getReferenceUrl())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateAdvertisementDto dto, Advertisement entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getContent());
        entity.setReferenceUrl(dto.getUrl());
        entity.setImageUrl(dto.getImageUrl());
    }

    private void mapToEntityForUpdate(UpdateAdvertisementDto dto, Advertisement entity) {
        boolean hasUpdates = false;

        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
            hasUpdates = true;
        }
        if (dto.getContent() != null) {
            entity.setDescription(dto.getContent());
            hasUpdates = true;
        }
        if (dto.getUrl() != null) {
            entity.setReferenceUrl(dto.getUrl());
            hasUpdates = true;
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
            hasUpdates = true;
        }

        if (!hasUpdates) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementDto> getLatestAdvertisements(Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (pageable.getSort().isUnsorted()) {
            effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Advertisement> advertisementPage = advertisementRepository.findAll(effectivePageable);
        return advertisementPage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdvertisementDto> getAdvertisementById(String id) {
        return advertisementRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public AdvertisementDto createAdvertisement(CreateAdvertisementDto createDto) {
        if (createDto == null) {
            throw new IllegalArgumentException("CreateAdvertisementDto cannot be null.");
        }
        Advertisement advertisement = new Advertisement();
        mapToEntityForCreate(createDto, advertisement);
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        return mapToDto(savedAdvertisement);
    }

    @Override
    @Transactional
    public AdvertisementDto updateAdvertisement(String id, UpdateAdvertisementDto updateDto) {
        if (updateDto == null) {
            throw new IllegalArgumentException("UpdateAdvertisementDto cannot be null.");
        }

        if (updateDto.getTitle() == null &&
                updateDto.getContent() == null &&
                updateDto.getUrl() == null &&
                updateDto.getImageUrl() == null) {
            throw new IllegalArgumentException("No updateable fields provided in UpdateAdvertisementDto.");
        }

        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement with ID " + id + " not found"));

        mapToEntityForUpdate(updateDto, advertisement);

        Advertisement updatedAdvertisement = advertisementRepository.save(advertisement);
        return mapToDto(updatedAdvertisement);
    }

    @Override
    @Transactional
    public void deleteAdvertisement(String id) {
        log.info("Delete PathVariable: {}", id);
        if (!advertisementRepository.existsById(id)) {
            throw new EntityNotFoundException("Advertisement with ID " + id + " not found");
        }
        advertisementRepository.deleteById(id);
    }
}
