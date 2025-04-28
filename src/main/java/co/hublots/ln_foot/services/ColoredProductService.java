package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.ColoredProduct;
import java.util.List;


public interface ColoredProductService {
    List<ColoredProduct> getAllColoredProducts();

    ColoredProduct getColoredProductById(String id);

    ColoredProduct createColoredProduct(ColoredProduct color);

    ColoredProduct updateColoredProduct(String id, ColoredProduct color);

    void deleteColoredProduct(String id);
}