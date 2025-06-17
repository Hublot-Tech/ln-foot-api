package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.models.Advertisement;
import co.hublots.ln_foot.repositories.AdvertisementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page; // Ensure Page is imported
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // Ensure Pageable is imported
import org.springframework.data.domain.Sort;

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
class AdvertisementServiceImplTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    private AdvertisementServiceImpl advertisementService;

    @BeforeEach
    void setUp() {
        advertisementService = new AdvertisementServiceImpl(advertisementRepository);
    }

    private Advertisement createMockAdvertisement(String id, String title) {
        return Advertisement.builder()
                .id(id)
                .title(title)
                .description("Mock Description")
                .referenceUrl("http://example.com/mock")
                .imageUrl("http://example.com/mock.png")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getLatestAdvertisements_returnsPagedDtos_withDefaultSort() {
        // Arrange
        Pageable requestedPageable = PageRequest.of(0, 5); // Unsorted
        Pageable expectedRepoPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Advertisement mockAd = createMockAdvertisement(UUID.randomUUID().toString(), "Latest Ad");
        when(advertisementRepository.findAll(expectedRepoPageable)).thenReturn(new PageImpl<>(List.of(mockAd), expectedRepoPageable, 1));

        // Act
        Page<AdvertisementDto> result = advertisementService.getLatestAdvertisements(requestedPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockAd.getTitle(), result.getContent().get(0).getTitle());
        verify(advertisementRepository).findAll(expectedRepoPageable);
    }

    @Test
    void getLatestAdvertisements_returnsPagedDtos_withClientSort() {
        // Arrange
        Pageable clientPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "title"));
        // Service should use this sort if present, not override with default
        Advertisement mockAd = createMockAdvertisement(UUID.randomUUID().toString(), "Ad A");
        when(advertisementRepository.findAll(clientPageable)).thenReturn(new PageImpl<>(List.of(mockAd), clientPageable, 1));

        // Act
        Page<AdvertisementDto> result = advertisementService.getLatestAdvertisements(clientPageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockAd.getTitle(), result.getContent().get(0).getTitle());
        verify(advertisementRepository).findAll(clientPageable);
    }

    @Test
    void getLatestAdvertisements_returnsEmptyPage_whenNoAds() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(advertisementRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        // Act
        Page<AdvertisementDto> result = advertisementService.getLatestAdvertisements(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(advertisementRepository).findAll(pageable);
    }

    @Test
    void getAdvertisementById_whenFound_returnsOptionalDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Advertisement mockAd = createMockAdvertisement(id, "Found Ad");
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(mockAd));

        // Act
        Optional<AdvertisementDto> result = advertisementService.getAdvertisementById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockAd.getTitle(), result.get().getTitle());
        assertEquals(mockAd.getDescription(), result.get().getContent()); // DTO maps description to content
        assertEquals(mockAd.getReferenceUrl(), result.get().getUrl());   // DTO maps referenceUrl to url
        verify(advertisementRepository).findById(id);
    }

    @Test
    void getAdvertisementById_whenNotFound_returnsEmptyOptional() {
        // Arrange
        String id = "nonexistent-id";
        when(advertisementRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<AdvertisementDto> result = advertisementService.getAdvertisementById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(advertisementRepository).findById(id);
    }

    @Test
    void createAdvertisement_savesAndReturnsDto() {
        // Arrange
        CreateAdvertisementDto createDto = CreateAdvertisementDto.builder()
                .title("New Ad")
                .content("New Content") // DTO uses content
                .url("http://new.example.com") // DTO uses url
                .imageUrl("http://new.example.com/new.png")
                .build();

        ArgumentCaptor<Advertisement> advertisementArgumentCaptor = ArgumentCaptor.forClass(Advertisement.class);

        // When save is called, return the captured argument but ensure it has an ID and timestamps like JPA would
        when(advertisementRepository.save(advertisementArgumentCaptor.capture())).thenAnswer(invocation -> {
            Advertisement savedAd = invocation.getArgument(0);
            savedAd.setId(UUID.randomUUID().toString());
            savedAd.setCreatedAt(LocalDateTime.now());
            savedAd.setUpdatedAt(LocalDateTime.now());
            return savedAd;
        });

        // Act
        AdvertisementDto resultDto = advertisementService.createAdvertisement(createDto);

        // Assert
        assertNotNull(resultDto);
        assertNotNull(resultDto.getId());
        assertEquals(createDto.getTitle(), resultDto.getTitle());
        assertEquals(createDto.getContent(), resultDto.getContent());
        assertEquals(createDto.getUrl(), resultDto.getUrl());
        assertEquals(createDto.getImageUrl(), resultDto.getImageUrl());
        assertNotNull(resultDto.getCreatedAt());
        assertNotNull(resultDto.getUpdatedAt());

        Advertisement capturedAd = advertisementArgumentCaptor.getValue();
        assertEquals(createDto.getTitle(), capturedAd.getTitle());
        assertEquals(createDto.getContent(), capturedAd.getDescription()); // Check mapping
        assertEquals(createDto.getUrl(), capturedAd.getReferenceUrl());   // Check mapping

        verify(advertisementRepository).save(any(Advertisement.class));
    }

    @Test
    void updateAdvertisement_whenFound_updatesAndReturnsDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Advertisement existingAd = createMockAdvertisement(id, "Old Title");
        when(advertisementRepository.findById(id)).thenReturn(Optional.of(existingAd));

        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder()
                .title("Updated Title")
                .content("Updated Content") // DTO uses content
                .build();

        // Mock save to return the updated entity
        // In a real scenario, the save method might return the same instance passed to it after modification
        when(advertisementRepository.save(any(Advertisement.class))).thenAnswer(invocation -> {
            Advertisement adToSave = invocation.getArgument(0);
            adToSave.setUpdatedAt(LocalDateTime.now()); // Simulate @UpdateTimestamp
            return adToSave;
        });

        // Act
        AdvertisementDto resultDto = advertisementService.updateAdvertisement(id, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(id, resultDto.getId());
        assertEquals("Updated Title", resultDto.getTitle());
        assertEquals("Updated Content", resultDto.getContent()); // Check mapping from DTO to entity then back to DTO
        assertTrue(resultDto.getUpdatedAt().isAfter(existingAd.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1))); // Updated time

        verify(advertisementRepository).findById(id);
        ArgumentCaptor<Advertisement> adCaptor = ArgumentCaptor.forClass(Advertisement.class);
        verify(advertisementRepository).save(adCaptor.capture());
        assertEquals("Updated Title", adCaptor.getValue().getTitle());
        assertEquals("Updated Content", adCaptor.getValue().getDescription());
    }

    @Test
    void updateAdvertisement_whenNotFound_throwsEntityNotFoundException() {
        // Arrange
        String id = UUID.randomUUID().toString();
        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder().title("Valid title").build();
        when(advertisementRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Exception thrownException = null;
        try {
            advertisementService.updateAdvertisement(id, updateDto);
        } catch (Exception e) {
            thrownException = e;
        }

        // Assert
        assertNotNull(thrownException, "Expected an exception to be thrown");
        assertTrue(thrownException instanceof EntityNotFoundException,
                "Expected EntityNotFoundException, but got " + thrownException.getClass().getName());
        verify(advertisementRepository).findById(id); // Verify findById was called
        verify(advertisementRepository, never()).save(any(Advertisement.class)); // Verify save was not called
    }

    @Test
    void updateAdvertisement_partialUpdate_updatesOnlyProvidedFields() {
        // Arrange
        String id = UUID.randomUUID().toString();
        Advertisement existingAd = createMockAdvertisement(id, "Original Title");
        existingAd.setDescription("Original Description");
        existingAd.setReferenceUrl("http://original.url");

        when(advertisementRepository.findById(id)).thenReturn(Optional.of(existingAd));
        when(advertisementRepository.save(any(Advertisement.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder()
                .title("New Title Only") // Only title is updated
                .build();

        // Act
        AdvertisementDto resultDto = advertisementService.updateAdvertisement(id, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals("New Title Only", resultDto.getTitle());
        assertEquals(existingAd.getDescription(), resultDto.getContent()); // Should remain original
        assertEquals(existingAd.getReferenceUrl(), resultDto.getUrl());   // Should remain original

        ArgumentCaptor<Advertisement> adCaptor = ArgumentCaptor.forClass(Advertisement.class);
        verify(advertisementRepository).save(adCaptor.capture());
        assertEquals("New Title Only", adCaptor.getValue().getTitle());
        assertEquals("Original Description", adCaptor.getValue().getDescription());
    }


    @Test
    void deleteAdvertisement_whenFound_deletesAdvertisement() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(advertisementRepository.existsById(id)).thenReturn(true);
        doNothing().when(advertisementRepository).deleteById(id);

        // Act
        assertDoesNotThrow(() -> advertisementService.deleteAdvertisement(id));

        // Assert
        verify(advertisementRepository).existsById(id);
        verify(advertisementRepository).deleteById(id);
    }

    @Test
    void deleteAdvertisement_whenNotFound_throwsEntityNotFoundException() {
        // Arrange
        String id = "nonexistent-id";
        when(advertisementRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> advertisementService.deleteAdvertisement(id));
        verify(advertisementRepository).existsById(id);
        verify(advertisementRepository, never()).deleteById(id);
    }
}
