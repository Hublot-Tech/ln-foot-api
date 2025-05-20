package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // Update sizes
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
    public ProductVariant updateProductVariant(String id, ProductVariant productVariant) {
        ProductVariant existingProductVariant = productVariantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        Optional.of(productVariant.getImageUrl()).ifPresent(existingProductVariant::setImageUrl);
        Optional.of(productVariant.getStockQuantity()).ifPresent(existingProductVariant::setStockQuantity);
        Optional.of(productVariant.getPrice()).ifPresent(existingProductVariant::setPrice);
        // Update sizes
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
}