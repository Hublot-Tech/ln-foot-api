package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;

import java.util.List;
import java.util.Optional;

public interface AdvertisementService {
    List<AdvertisementDto> getLatestAdvertisements();
    Optional<AdvertisementDto> getAdvertisementById(String id);
    AdvertisementDto createAdvertisement(CreateAdvertisementDto createDto);
    AdvertisementDto updateAdvertisement(String id, UpdateAdvertisementDto updateDto);
    void deleteAdvertisement(String id);
}
