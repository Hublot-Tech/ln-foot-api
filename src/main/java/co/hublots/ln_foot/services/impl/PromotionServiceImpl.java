// src/main/java/co/hublots/ln_foot/services/impl/PromotionServiceImpl.java
package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.PromotionDto;
import co.hublots.ln_foot.models.Promotion;
import co.hublots.ln_foot.repositories.PromotionRepository;
import co.hublots.ln_foot.services.PromotionService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public Promotion getPromotionById(UUID id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Override
    public Promotion createPromotion(PromotionDto promotionDto) {
        Promotion promotion = promotionDto.toEntity();
        return promotionRepository.save(promotion);
    }

    @Override
    public List<Promotion> createPromotions(List<PromotionDto> promotionDtos) {
        List<Promotion> promotions = promotionDtos.stream()
                .map(promotionDto -> promotionDto.toEntity())
                .map(promotionRepository::save)
                .collect(Collectors.toList());
        return promotions;
    }

    @Override
    public Promotion updatePromotion(UUID id, PromotionDto promotionDto) {
        Promotion promotion = promotionDto.toEntity();
        promotion.setId(id);
        return promotionRepository.save(promotion);
    }

    @Override
    public void deletePromotion(UUID id) {
        promotionRepository.deleteById(id);
    }
}