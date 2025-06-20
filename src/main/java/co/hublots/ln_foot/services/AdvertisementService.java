package co.hublots.ln_foot.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;

public interface AdvertisementService {
    Page<AdvertisementDto> getLatestAdvertisements(Pageable pageable); // Changed signature
    Optional<AdvertisementDto> getAdvertisementById(String id);
    AdvertisementDto createAdvertisement(CreateAdvertisementDto createDto);
    AdvertisementDto updateAdvertisement(String id, UpdateAdvertisementDto updateDto);
    void deleteAdvertisement(String id);
}
