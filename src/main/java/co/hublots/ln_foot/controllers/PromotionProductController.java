package co.hublots.ln_foot.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.PromotionProductDto;
import co.hublots.ln_foot.models.PromotionProduct;
import co.hublots.ln_foot.services.PromotionProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/PromotionProducts")
public class PromotionProductController {

    private final PromotionProductService PromotionProductService;

    @GetMapping
    public List<PromotionProduct> getAllPromotionProducts() {
        return PromotionProductService.getAllPromotionProducts();
    }

    @GetMapping("/{id}")
    public PromotionProduct getPromotionProductById(@PathVariable String id) {
        return PromotionProductService.getPromotionProductById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PromotionProduct createPromotionProduct(
            @Valid @RequestBody PromotionProductDto PromotionProductDto) {
        return PromotionProductService.createPromotionProduct(PromotionProductDto);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PromotionProduct> createPromotionProducts(
            @Valid @RequestBody List<PromotionProductDto> PromotionProductDtos) {
       return PromotionProductService.createPromotionProducts(PromotionProductDtos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PromotionProduct updatePromotionProduct(
            @PathVariable String id,
            @Valid @RequestBody PromotionProductDto PromotionProductDto) {
        return PromotionProductService.createPromotionProduct(PromotionProductDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePromotionProduct(@PathVariable String id) {
        PromotionProductService.deletePromotionProduct(id);
    }
}
