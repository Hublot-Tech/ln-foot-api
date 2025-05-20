package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.repositories.ColoredProductRepository;
import co.hublots.ln_foot.services.ColoredProductService;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ColoredProductServiceImpl implements ColoredProductService {
    
    private final ColoredProductRepository coloredProductRepository;

    @Override
    public List<ColoredProduct> getAllColoredProducts() {
        return coloredProductRepository.findAll();
    }

    @Override
    public ColoredProduct getColoredProductById(String id) {
        return coloredProductRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
    }

    @Override
    public ColoredProduct createColoredProduct(ColoredProduct color) {
        return coloredProductRepository.save(color);
    }

    @Override
    public ColoredProduct updateColoredProduct(String id, ColoredProduct color) {
        ColoredProduct existingColor = coloredProductRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        Optional.of(color.getImageUrl()).ifPresent(existingColor::setImageUrl);
        Optional.of(color.getStockQuantity()).ifPresent(existingColor::setStockQuantity);
        Optional.of(color.getPrice()).ifPresent(existingColor::setPrice);
        Optional.of(color.getProduct()).ifPresent(existingColor::setProduct);
        return coloredProductRepository.save(existingColor);
    }

    @Override
    public void deleteColoredProduct(String id) {
        coloredProductRepository.deleteById(id);
    }

    @Override
    public List<ColoredProduct> getColoredProductsByIds(List<String> ids) {
        return coloredProductRepository.findByIdIn(ids);
    }
}