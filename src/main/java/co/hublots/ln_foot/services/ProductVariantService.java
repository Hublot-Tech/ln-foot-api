package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.ProductVariant;
import java.util.List;


public interface ProductVariantService {
    List<ProductVariant> getAllProductVariants();
    List<ProductVariant> getProductVariantsByIds(List<String> ids);
    List<ProductVariant> getProductVariantsByProductId(String productId);

    ProductVariant getProductVariantById(String id);

    ProductVariant createProductVariant(ProductVariant color);

    List<ProductVariant> createProductVariants(List<ProductVariant> colors);

    ProductVariant updateProductVariant(String id, ProductVariant color);

    void deleteProductVariant(String id);
}