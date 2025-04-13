package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.models.Color;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.CategoryRepository;
import co.hublots.ln_foot.repositories.ColorRepository;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
            SizeRepository sizeRepository, ColorRepository colorRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        Optional.ofNullable(product.getCategories()).ifPresent(categories -> product.setCategories(categories));
        Optional.ofNullable(product.getSizes()).ifPresent(sizes -> product.setSizes(sizes));
        Optional.ofNullable(product.getColors()).ifPresent(colors -> product.setColors(colors));

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
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
        if (product.getColors() != null) {
            List<Color> colors = product.getColors().stream()
                    .map(color -> colorRepository.findById(color.getId())
                            .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + color.getId())))
                    .collect(Collectors.toList());
            existingProduct.setColors(colors);
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
