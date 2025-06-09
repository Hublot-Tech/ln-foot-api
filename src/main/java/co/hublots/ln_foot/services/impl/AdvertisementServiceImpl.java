package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.services.AdvertisementService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    @Override
    public List<AdvertisementDto> getLatestAdvertisements() {
        // Mock implementation
        return Collections.emptyList();
    }

    @Override
    public Optional<AdvertisementDto> getAdvertisementById(String id) {
        // Mock implementation
        // Example of returning a specific mock DTO if needed for testing:
        /*
        if ("test-id".equals(id)) {
            return Optional.of(AdvertisementDto.builder()
                .id(id)
                .title("Test Ad")
                .content("This is a test ad.")
                .url("http://example.com")
                .imageUrl("http://example.com/image.png")
                .startDate(OffsetDateTime.now().minusDays(1))
                .endDate(OffsetDateTime.now().plusDays(6))
                .priority(1)
                .status("active")
                .createdAt(OffsetDateTime.now().minusDays(2))
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .build());
        }
        */
        return Optional.empty();
    }

    @Override
    public AdvertisementDto createAdvertisement(CreateAdvertisementDto createDto) {
        // Mock implementation
        return AdvertisementDto.builder()
                .id(UUID.randomUUID().toString())
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .url(createDto.getUrl())
                .imageUrl(createDto.getImageUrl())
                .startDate(createDto.getStartDate())
                .endDate(createDto.getEndDate())
                .priority(createDto.getPriority() != null ? createDto.getPriority() : 1)
                .status(createDto.getStatus() != null ? createDto.getStatus() : "active")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public AdvertisementDto updateAdvertisement(String id, UpdateAdvertisementDto updateDto) {
        // Mock implementation: assumes an existing ad is fetched and then updated
        return AdvertisementDto.builder()
                .id(id)
                .title(updateDto.getTitle() != null ? updateDto.getTitle() : "Original Title")
                .content(updateDto.getContent() != null ? updateDto.getContent() : "Original Content")
                .url(updateDto.getUrl() != null ? updateDto.getUrl() : "http://original.url")
                .imageUrl(updateDto.getImageUrl() != null ? updateDto.getImageUrl() : "http://original.image/img.png")
                .startDate(updateDto.getStartDate() != null ? updateDto.getStartDate() : OffsetDateTime.now().minusDays(5))
                .endDate(updateDto.getEndDate() != null ? updateDto.getEndDate() : OffsetDateTime.now().plusDays(5))
                .priority(updateDto.getPriority() != null ? updateDto.getPriority() : 1)
                .status(updateDto.getStatus() != null ? updateDto.getStatus() : "active")
                .createdAt(OffsetDateTime.now().minusDays(10)) // Should be original creation date
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public void deleteAdvertisement(String id) {
        // Mock implementation: Do nothing
        System.out.println("Deleting advertisement with id: " + id);
    }
}
