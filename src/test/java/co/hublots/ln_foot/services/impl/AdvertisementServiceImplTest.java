package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AdvertisementServiceImplTest {
    private AdvertisementServiceImpl advertisementService;

    @BeforeEach
    void setUp() {
        advertisementService = new AdvertisementServiceImpl();
    }

    @Test
    void getLatestAdvertisements_returnsEmptyList() {
        List<AdvertisementDto> result = advertisementService.getLatestAdvertisements();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAdvertisementById_returnsEmptyOptional_forRandomId() {
        Optional<AdvertisementDto> result = advertisementService.getAdvertisementById("nonexistent-id");
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void createAdvertisement_returnsMockDto() {
        CreateAdvertisementDto createDto = CreateAdvertisementDto.builder()
                .title("Test Ad")
                .content("Test Content")
                .url("http://example.com")
                .imageUrl("http://example.com/image.png")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(7))
                .priority(1)
                .status("active")
                .build();

        AdvertisementDto result = advertisementService.createAdvertisement(createDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Ad", result.getTitle());
        assertEquals("Test Content", result.getContent());
        assertEquals("http://example.com", result.getUrl());
        assertEquals("http://example.com/image.png", result.getImageUrl());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals("active", result.getStatus());
    }

    @Test
    void updateAdvertisement_returnsMockDto() {
        String adId = "test-ad-id";
        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder()
                .title("Updated Test Ad")
                .content("Updated Test Content")
                .build();

        AdvertisementDto result = advertisementService.updateAdvertisement(adId, updateDto);

        assertNotNull(result);
        assertEquals(adId, result.getId());
        assertEquals("Updated Test Ad", result.getTitle());
        assertEquals("Updated Test Content", result.getContent());
        // Check that other fields got some default mock values
        assertNotNull(result.getUrl());
        assertNotNull(result.getImageUrl());
        assertNotNull(result.getCreatedAt()); // Should be some mock original creation date
        assertNotNull(result.getUpdatedAt()); // Should be "now"
    }

    @Test
    void updateAdvertisement_usesOriginalValues_whenUpdateDtoFieldsAreNull() {
        String adId = "test-ad-id-2";
        UpdateAdvertisementDto updateDtoWithNulls = new UpdateAdvertisementDto(); // All fields null

        AdvertisementDto result = advertisementService.updateAdvertisement(adId, updateDtoWithNulls);

        assertNotNull(result);
        assertEquals(adId, result.getId());
        // These should be the "Original Title" etc from the mock implementation
        assertEquals("Original Title", result.getTitle());
        assertEquals("Original Content", result.getContent());
        assertNotNull(result.getUpdatedAt());
    }


    @Test
    void deleteAdvertisement_completesWithoutError() {
        String adId = "test-ad-id-to-delete";
        // The mock method is void and just prints, so we just call it.
        // If it threw an exception, this test would fail.
        assertDoesNotThrow(() -> advertisementService.deleteAdvertisement(adId));
    }
}
