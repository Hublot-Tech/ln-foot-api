package co.hublots.ln_foot.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.BulkProductVariantDto;
import co.hublots.ln_foot.dto.ProductVariantDto;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.services.ProductVariantService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-variants")
public class ProductVariantController {
    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<ProductVariantDto>> getProductVariants(@Param("productId") Optional<String> productId) {
        if (productId.isPresent()) {
            List<ProductVariant> productVariants = productVariantService.getProductVariantsByProductId(productId.get());
            return new ResponseEntity<>(
                    productVariants.stream().map(ProductVariantDto::fromEntity).collect(Collectors.toList()),
                    HttpStatus.OK);
        }

        // If no productId is provided, return all product variants
        List<ProductVariant> productVariants = productVariantService.getAllProductVariants();
        return new ResponseEntity<>(
                productVariants.stream().map(ProductVariantDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDto> getProductVariant(@PathVariable String id) {
        ProductVariant productVariant = productVariantService.getProductVariantById(id);
        return new ResponseEntity<>(ProductVariantDto.fromEntity(productVariant), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantDto> createProductVariant(
            @RequestBody @Valid ProductVariantDto productVariantDto) {

        ProductVariant productVariant = productVariantDto.toEntity();

        ProductVariant createdColor = productVariantService.createProductVariant(productVariant);
        return new ResponseEntity<>(ProductVariantDto.fromEntity(createdColor), HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductVariantDto>> createProductVariants(
            @Valid @RequestBody BulkProductVariantDto bulkProductVariantDto) {
        List<ProductVariantDto> variantDtos = bulkProductVariantDto.getVariants();
        log.debug("Bulk Object: " + bulkProductVariantDto);
        // Convert to entities and assign images
        List<ProductVariant> variants = new ArrayList<>();
        for (int i = 0; i < variantDtos.size(); i++) {
            ProductVariantDto dto = variantDtos.get(i);
            ProductVariant entity = dto.toEntity();
            variants.add(entity);
        }

        // Persist in bulk
        List<ProductVariant> saved = productVariantService.createProductVariants(variants);

        return new ResponseEntity<>(
                saved.stream().map(ProductVariantDto::fromEntity).toList(),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantDto> updateProductVariant(@PathVariable String id,
            @RequestBody @Valid ProductVariantDto productVariantDto) {
        try {
            ProductVariant productVariant = productVariantDto.toEntity();

            ProductVariant updatedColor = productVariantService.updateProductVariant(id, productVariant);
            return new ResponseEntity<>(ProductVariantDto.fromEntity(updatedColor), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable String id) {
        productVariantService.deleteProductVariant(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}