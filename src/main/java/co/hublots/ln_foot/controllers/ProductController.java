package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.ProductDto;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.services.ProductService;
import co.hublots.ln_foot.services.CategoryService;
import co.hublots.ln_foot.services.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import co.hublots.ln_foot.services.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private final ProductService productService;
    @Autowired
    private final CategoryService categoryService;
    @Autowired
    private final SizeService sizeService;
    @Autowired
    private final ColorService colorService;

    @GetMapping
    public List<ProductDto> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return products.stream()
                .map(ProductDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return new ResponseEntity<>(ProductDto.fromEntity(product), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = productDto.toEntity(categoryService, sizeService, colorService);
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(ProductDto.fromEntity(createdProduct), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        try {
            Product product = productDto.toEntity(categoryService, sizeService, colorService);
            Product updatedProduct = productService.updateProduct(id, product);
            return new ResponseEntity<>(ProductDto.fromEntity(updatedProduct), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
