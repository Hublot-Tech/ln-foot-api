package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.ColoredProductRepository;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.ColoredProductService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColoredProductServiceImpl implements ColoredProductService {

    private final SizeRepository sizeRepository;
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
    public ColoredProduct createColoredProduct(ColoredProduct coloredProduct) {
        // Update sizes
        if (coloredProduct.getSizes() != null) {
            List<Size> sizes = coloredProduct.getSizes().stream()
                    .map(size -> sizeRepository.findByNameIgnoreCase(size.getName())
                            .orElseGet(() -> sizeRepository.save(size)))
                    .collect(Collectors.toList());
            coloredProduct.setSizes(sizes);
        }

        return coloredProductRepository.save(coloredProduct);
    }

    @Override
    public ColoredProduct updateColoredProduct(String id, ColoredProduct coloredProduct) {
        ColoredProduct existingColoredProduct = coloredProductRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        Optional.of(coloredProduct.getImageUrl()).ifPresent(existingColoredProduct::setImageUrl);
        Optional.of(coloredProduct.getStockQuantity()).ifPresent(existingColoredProduct::setStockQuantity);
        Optional.of(coloredProduct.getPrice()).ifPresent(existingColoredProduct::setPrice);
        // Update sizes
        if (coloredProduct.getSizes() != null) {
            List<Size> sizes = coloredProduct.getSizes().stream()
                    .map(size -> sizeRepository.findById(size.getId())
                            .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + size.getId())))
                    .collect(Collectors.toList());
            existingColoredProduct.setSizes(sizes);
        }

        return coloredProductRepository.save(existingColoredProduct);
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