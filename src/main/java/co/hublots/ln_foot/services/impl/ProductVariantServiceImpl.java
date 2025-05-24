package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.ProductVariantRepository;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.ProductVariantService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final SizeRepository sizeRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<ProductVariant> getAllProductVariants() {
        return productVariantRepository.findAll();
    }

    @Override
    public ProductVariant getProductVariantById(String id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
    }

    @Override
    public ProductVariant createProductVariant(ProductVariant productVariant) {
        if (productVariant.getSizes() != null) {
            List<Size> sizes = productVariant.getSizes().stream()
                    .map(size -> sizeRepository.findByNameIgnoreCase(size.getName())
                            .orElseGet(() -> sizeRepository.save(size)))
                    .collect(Collectors.toList());
            productVariant.setSizes(sizes);
        }

        return productVariantRepository.save(productVariant);
    }

    @Override
    public List<ProductVariant> createProductVariants(List<ProductVariant> productVariants) {
        Set<String> sizeNames = productVariants.stream()
                .flatMap(variant -> variant.getSizes() != null ? variant.getSizes().stream() : Stream.empty())
                .map(size -> size.getName().toLowerCase())
                .collect(Collectors.toSet());

        Map<String, Size> existingSizes = sizeRepository.findAllByNameInIgnoreCase(sizeNames).stream()
                .collect(Collectors.toMap(size -> size.getName().toLowerCase(), Function.identity()));

        for (ProductVariant variant : productVariants) {
            if (variant.getSizes() != null) {
                List<Size> resolvedSizes = variant.getSizes().stream()
                        .map(size -> {
                            String lowerCaseName = size.getName().toLowerCase();
                            return existingSizes.computeIfAbsent(lowerCaseName, k -> sizeRepository.save(size));
                        })
                        .collect(Collectors.toList());

                variant.setSizes(resolvedSizes);
            }
        }

        return productVariantRepository.saveAll(productVariants);
    }

    @Override
    public ProductVariant updateProductVariant(String id, ProductVariant productVariant) {
        ProductVariant existingProductVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        Optional.of(productVariant.getImageUrl()).ifPresent(existingProductVariant::setImageUrl);
        Optional.of(productVariant.getStockQuantity()).ifPresent(existingProductVariant::setStockQuantity);
        Optional.of(productVariant.getPrice()).ifPresent(existingProductVariant::setPrice);

        if (productVariant.getSizes() != null) {
            List<Size> sizes = productVariant.getSizes().stream()
                    .map(size -> sizeRepository.findById(size.getId())
                            .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + size.getId())))
                    .collect(Collectors.toList());
            existingProductVariant.setSizes(sizes);
        }

        return productVariantRepository.save(existingProductVariant);
    }

    @Override
    public void deleteProductVariant(String id) {
        productVariantRepository.deleteById(id);
    }

    @Override
    public List<ProductVariant> getProductVariantsByIds(List<String> ids) {
        return productVariantRepository.findByIdIn(ids);
    }

    @Override
    public List<ProductVariant> getProductVariantsByProductId(String productId) {
        return productVariantRepository.findAllByProductId(productId);
    }
}