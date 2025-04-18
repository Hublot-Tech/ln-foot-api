package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.ProductDto;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

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
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
        try {
            Product product = productService.getProductById(id);
            return new ResponseEntity<>(
                    ProductDto.fromEntity(product),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody ProductDto productDto) {
        Product product = productDto.toEntity();
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(
                ProductDto.fromEntity(createdProduct),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDto productDto) {
        try {
            Product product = productDto.toEntity();
            Product updatedProduct = productService.updateProduct(id, product);
            return new ResponseEntity<>(
                    ProductDto.fromEntity(updatedProduct),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
