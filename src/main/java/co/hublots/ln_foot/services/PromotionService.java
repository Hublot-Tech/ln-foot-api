// src/main/java/co/hublots/ln_foot/services/PromotionService.java
package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.PromotionDto;
import co.hublots.ln_foot.models.Promotion;
import java.util.List;


public interface PromotionService {
    List<Promotion> getAllPromotions();

    Promotion getPromotionById(String id);

    Promotion createPromotion(PromotionDto promotionDto);

    List<Promotion> createPromotions(List<PromotionDto> promotionDtos);

    Promotion updatePromotion(String id, PromotionDto promotionDto);

    void deletePromotion(String id);
}