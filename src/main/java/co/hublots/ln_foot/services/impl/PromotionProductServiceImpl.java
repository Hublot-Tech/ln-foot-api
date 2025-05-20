package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.hublots.ln_foot.dto.PromotionProductDto;
import co.hublots.ln_foot.models.PromotionProduct;
import co.hublots.ln_foot.repositories.PromotionProductRepository;
import co.hublots.ln_foot.services.PromotionProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionProductServiceImpl implements PromotionProductService {

    @Autowired
    private final PromotionProductRepository PromotionProductRepository;

    @Override
    public List<PromotionProduct> getAllPromotionProducts() {
        return PromotionProductRepository.findAll();
    }

    @Override
    public PromotionProduct getPromotionProductById(String id) {
        return PromotionProductRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Override
    public PromotionProduct createPromotionProduct(PromotionProductDto PromotionProductDto) {
        PromotionProduct PromotionProduct = PromotionProductDto.toEntity();
        return PromotionProductRepository.save(PromotionProduct);
    }

    @Override
    @Transactional
    public List<PromotionProduct> createPromotionProducts(List<PromotionProductDto> PromotionProductDtos) {
        List<PromotionProduct> promotionProducts = PromotionProductDtos.stream()
                .map(PromotionProductDto -> PromotionProductDto.toEntity())
                .map(PromotionProductRepository::save)
                .collect(Collectors.toList());
        return promotionProducts;
    }

    @Override
    public PromotionProduct updatePromotionProduct(String id, PromotionProductDto PromotionProductDto) {
        PromotionProduct PromotionProduct = PromotionProductDto.toEntity();
        PromotionProduct.setId(id);
        return PromotionProductRepository.save(PromotionProduct);
    }

    @Override
    public void deletePromotionProduct(String id) {
        PromotionProductRepository.deleteById(id);
    }
}