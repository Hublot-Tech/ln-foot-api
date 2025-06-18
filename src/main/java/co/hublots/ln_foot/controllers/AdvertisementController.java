package co.hublots.ln_foot.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.services.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("/latest")
    public ResponseEntity<Page<AdvertisementDto>> getLatestAdvertisements(Pageable pageable) { // Changed signature
        Page<AdvertisementDto> advertisementsPage = advertisementService.getLatestAdvertisements(pageable);
        return ResponseEntity.ok(advertisementsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDto> getAdvertisementById(@PathVariable String id) {
        log.info("PathVariable: {}", id);
        return advertisementService.getAdvertisementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdvertisementDto> createAdvertisement(@Valid @RequestBody CreateAdvertisementDto createDto) {
        AdvertisementDto createdAd = advertisementService.createAdvertisement(createDto);
        return new ResponseEntity<>(createdAd, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdvertisement(@PathVariable String id,
            @Valid @RequestBody UpdateAdvertisementDto updateDto) {
        log.info("PathVariable: {}, DTO: {}", id, updateDto);

        AdvertisementDto updatedAdvertisement = advertisementService.updateAdvertisement(id, updateDto);
        return new ResponseEntity<>(updatedAdvertisement, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable String id) {
        log.info("PathVariable: {}", id);
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.noContent().build();
    }
}
