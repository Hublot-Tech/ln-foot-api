package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.services.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("/latest")
    public List<AdvertisementDto> getLatestAdvertisements() {
        return advertisementService.getLatestAdvertisements();
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
    public ResponseEntity<AdvertisementDto> updateAdvertisement(@PathVariable String id, @RequestBody UpdateAdvertisementDto updateDto) {
        AdvertisementDto updatedAd = advertisementService.updateAdvertisement(id, updateDto);
        // In a real scenario, updateAdvertisement might return Optional or throw an exception if not found
        // For this mock, we assume it always succeeds if it doesn't throw.
        if (updatedAd != null) { // Basic check, service could return null if id not found in a more complex mock
            return ResponseEntity.ok(updatedAd);
        } else {
            // This case might not be reached with the current mock AdvertisementServiceImpl,
            // but good practice for a real implementation.
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable String id) {
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }
}
