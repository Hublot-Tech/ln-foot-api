// src/main/java/co/hublots/ln_foot/services/PromotionService.java
package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.PromotionDto;
import co.hublots.ln_foot.models.Promotion;
import java.util.List;
import java.util.UUID;

public interface PromotionService {
    List<Promotion> getAllPromotions();

    Promotion getPromotionById(UUID id);

    Promotion createPromotion(PromotionDto promotionDto);

    List<Promotion> createPromotions(List<PromotionDto> promotionDtos);

    Promotion updatePromotion(UUID id, PromotionDto promotionDto);

    void deletePromotion(UUID id);
}