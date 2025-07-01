package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.ProductDto;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.services.ProductService;
import co.hublots.ln_foot.services.ProductVariantService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(
                products
                        .stream()
                        .map(ProductDto::fromEntity)
                        .collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        Product product = productService.getProductById(id);
        return new ResponseEntity<>(
                ProductDto.fromEntity(product),
                HttpStatus.OK);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = productDto.toEntity();

        Product createdProduct = productService.createProduct(product);

        // creating default colored product
        productVariantService
                .createProductVariant(ProductVariant.builder()
                        .stockQuantity(product.getStockQuantity())
                        .sizes(product.getSizes())
                        .price(product.getPrice())
                        .colorCode("default")
                        .imageUrl(product.getImageUrl())
                        .product(product)
                        .build());

        return new ResponseEntity<>(
                ProductDto.fromEntity(createdProduct),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDto productDto) {
        Product product = productDto.toEntity();

        Product updatedProduct = productService.updateProduct(id, product);
        return new ResponseEntity<>(
                ProductDto.fromEntity(updatedProduct),
                HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
