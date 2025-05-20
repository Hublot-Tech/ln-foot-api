// src/main/java/co/hublots/ln_foot/services/PromotionProductService.java
package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.PromotionProductDto;
import co.hublots.ln_foot.models.PromotionProduct;
import jakarta.transaction.Transactional;

import java.util.List;


public interface PromotionProductService {
    List<PromotionProduct> getAllPromotionProducts();

    PromotionProduct getPromotionProductById(String id);

    PromotionProduct createPromotionProduct(PromotionProductDto PromotionProductDto);

    @Transactional
    List<PromotionProduct> createPromotionProducts(List<PromotionProductDto> PromotionProductDtos);

    PromotionProduct updatePromotionProduct(String id, PromotionProductDto PromotionProductDto);

    void deletePromotionProduct(String id);
}