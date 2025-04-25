package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.CategoryRepository;
import co.hublots.ln_foot.repositories.ColoredProductRepository;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ColoredProductRepository colorRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(String id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));

        // Update basic fields if present in DTO
        Optional.ofNullable(product.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(product.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(product.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(product.getImageUrl()).ifPresent(existingProduct::setImageUrl);
        Optional.ofNullable(product.getStockQuantity()).ifPresent(existingProduct::setStockQuantity);

        // Update categories
        if (product.getCategories() != null) {
            List<Category> categories = product.getCategories().stream()
                    .map(category -> categoryRepository.findById(category.getId())
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Category not found with id: " + category.getId())))
                    .collect(Collectors.toList());
            existingProduct.setCategories(categories);
        }

        // Update sizes
        if (product.getSizes() != null) {
            List<Size> sizes = product.getSizes().stream()
                    .map(size -> sizeRepository.findById(size.getId())
                            .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + size.getId())))
                    .collect(Collectors.toList());
            existingProduct.setSizes(sizes);
        }

        // Update colors
        if (product.getColoredProducts() != null) {
            List<ColoredProduct> colors = product.getColoredProducts().stream()
                    .map(color -> colorRepository.findById(color.getId())
                            .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + color.getId())))
                    .collect(Collectors.toList());
            existingProduct.setColoredProducts(colors);
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
