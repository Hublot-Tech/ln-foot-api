package co.hublots.ln_foot.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.PromotionDto;
import co.hublots.ln_foot.models.Promotion;
import co.hublots.ln_foot.repositories.PromotionRepository;
import co.hublots.ln_foot.repositories.ProductRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable Long id) {
        return promotionRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Promotion createPromotion(@Valid @RequestBody PromotionDto promotionDto) {
        Promotion promotion = promotionDto.toEntity(productRepository);
        return promotionRepository.save(promotion);
    }

    @PutMapping("/{id}")
    public Promotion updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionDto promotionDto) {
        Promotion promotion = promotionDto.toEntity(productRepository);
        promotion.setId(id);
        return promotionRepository.save(promotion);
    }

    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable Long id) {
        promotionRepository.deleteById(id);
    }
}
