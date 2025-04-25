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

    private final ColoredProductRepository colorRepository;

    @Override
    public List<ColoredProduct> getAllColors() {
        return colorRepository.findAll();
    }

    @Override
    public ColoredProduct getColoredProductById(String id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
    }

    @Override
    public ColoredProduct createColoredProduct(ColoredProduct color) {
        return colorRepository.save(color);
    }

    @Override
    public ColoredProduct updateColoredProduct(String id, ColoredProduct color) {
        ColoredProduct existingColor = colorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        Optional.of(color.getName()).ifPresent(existingColor::setName);
        return colorRepository.save(existingColor);
    }

    @Override
    public void deleteColoredProduct(String id) {
        colorRepository.deleteById(id);
    }
}