package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.services.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Added
import jakarta.persistence.EntityNotFoundException; // Added
import org.springframework.dao.DataAccessException; // Added
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added

import java.util.List; // Still needed for other methods if any, or can be removed if getLatest is the only list method

@Slf4j
@RestController
@RequestMapping("/api/v1/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("/latest")
    public ResponseEntity<Page<AdvertisementDto>> getLatestAdvertisements(Pageable pageable) { // Changed signature
        Page<AdvertisementDto> advertisementsPage = advertisementService.getLatestAdvertisements(pageable);
        return ResponseEntity.ok(advertisementsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDto> getAdvertisementById(@PathVariable String id) {
        return advertisementService.getAdvertisementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdvertisementDto> createAdvertisement(@RequestBody CreateAdvertisementDto createDto) {
        AdvertisementDto createdAd = advertisementService.createAdvertisement(createDto);
        return new ResponseEntity<>(createdAd, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdvertisement(@PathVariable String id, @Valid @RequestBody UpdateAdvertisementDto updateDto) {
        try {
            AdvertisementDto updatedAdvertisement = advertisementService.updateAdvertisement(id, updateDto);
            return ResponseEntity.ok(updatedAdvertisement);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent advertisement with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            log.error("Database error while updating advertisement with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("A database error occurred.");
        } catch (Exception e) {
            log.error("Unexpected error while updating advertisement with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable String id) {
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }
}
