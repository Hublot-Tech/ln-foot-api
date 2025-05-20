package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.PromotionProductDto;
import co.hublots.ln_foot.models.PromotionProduct;
import co.hublots.ln_foot.services.PromotionProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotion-products")
public class PromotionProductController {

    private final PromotionProductService promotionProductService;

    @GetMapping
    public ResponseEntity<List<PromotionProductDto>> getAllPromotionProducts() {
        List<PromotionProduct> promotionProducts = promotionProductService.getAllPromotionProducts();
        return new ResponseEntity<>(
                promotionProducts.stream().map(PromotionProductDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionProductDto> getPromotionProductById(@PathVariable String id) {
        PromotionProduct promotionProduct = promotionProductService.getPromotionProductById(id);
        return new ResponseEntity<>(
                PromotionProductDto.fromEntity(promotionProduct),
                HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionProductDto> createPromotionProduct(
            @Valid @RequestBody PromotionProductDto promotionProductDto) {
        PromotionProduct promotionProduct = promotionProductService.createPromotionProduct(promotionProductDto);
        return new ResponseEntity<>(
                PromotionProductDto.fromEntity(promotionProduct),
                HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromotionProductDto>> createPromotionProducts(
            @Valid @RequestBody List<PromotionProductDto> PromotionProductDtos) {
        List<PromotionProduct> promotionProducts = promotionProductService
                .createPromotionProducts(PromotionProductDtos);
        return new ResponseEntity<>(
                promotionProducts.stream().map(PromotionProductDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionProductDto> updatePromotionProduct(
            @PathVariable String id,
            @Valid @RequestBody PromotionProductDto promotionProductDto) {
        PromotionProduct promotionProduct = promotionProductService.createPromotionProduct(promotionProductDto);

        return new ResponseEntity<>(
                PromotionProductDto.fromEntity(promotionProduct),
                HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePromotionProduct(@PathVariable String id) {
        promotionProductService.deletePromotionProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
