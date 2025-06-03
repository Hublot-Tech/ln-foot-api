package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.ProductVariant;
import java.util.List;
import java.util.Optional;


public interface ProductVariantService {
    List<ProductVariant> getAllProductVariants();
    List<ProductVariant> getProductVariantsByIds(List<String> ids);
    List<ProductVariant> getProductVariantsByProductId(String productId);

    Optional<ProductVariant> getProductVariantById(String id);

    ProductVariant createProductVariant(ProductVariant color);

    List<ProductVariant> createProductVariants(List<ProductVariant> colors);

    ProductVariant updateProductVariant(String id, ProductVariant color);

    void deleteProductVariant(String id);
}