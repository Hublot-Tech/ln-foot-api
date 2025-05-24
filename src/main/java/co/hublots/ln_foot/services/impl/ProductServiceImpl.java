package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.models.Category;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.CategoryRepository;
import co.hublots.ln_foot.repositories.ProductRepository;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;

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
        if (product.getCategories() != null) {
            List<Category> categories = product.getCategories().stream()
                    .map(category -> categoryRepository.findByNameIgnoreCase(category.getName())
                            .orElseGet(() -> categoryRepository.save(category)))
                    .collect(Collectors.toList());
            product.setCategories(categories);
        }

        if (product.getSizes() != null) {
            List<Size> sizes = product.getSizes().stream()
                    .map(size -> sizeRepository.findByNameIgnoreCase(size.getName())
                            .orElseGet(() -> sizeRepository.save(size)))
                    .collect(Collectors.toList());
            product.setSizes(sizes);
        }

        log.debug(product.toString());
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(String id, Product product) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));

        Optional.ofNullable(product.getName()).ifPresent(existingProduct::setName);
        Optional.ofNullable(product.getDescription()).ifPresent(existingProduct::setDescription);
        Optional.ofNullable(product.getPrice()).ifPresent(existingProduct::setPrice);
        Optional.ofNullable(product.getImageUrl()).ifPresent(existingProduct::setImageUrl);
        Optional.ofNullable(product.getStockQuantity()).ifPresent(existingProduct::setStockQuantity);

        if (product.getCategories() != null) {
            List<Category> categories = product.getCategories().stream()
                    .map(category -> categoryRepository.findById(category.getId())
                            .orElseThrow(() -> new NoSuchElementException(
                                    "Category not found with id: " + category.getId())))
                    .collect(Collectors.toList());
            existingProduct.setCategories(categories);
        }

        if (product.getSizes() != null) {
            List<Size> sizes = product.getSizes().stream()
                    .map(size -> sizeRepository.findById(size.getId())
                            .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + size.getId())))
                    .collect(Collectors.toList());
            existingProduct.setSizes(sizes);
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}
