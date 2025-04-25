package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.dto.ProductDto;
import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.models.Product;
import co.hublots.ln_foot.services.ColoredProductService;
import co.hublots.ln_foot.services.MinioService;
import co.hublots.ln_foot.services.ProductService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final MinioService minioService;
    private final ProductService productService;
    private final ColoredProductService coloredProductService;

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
        try {
            Product product = productService.getProductById(id);
            return new ResponseEntity<>(
                    ProductDto.fromEntity(product),
                    HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
        Product product = productDto.toEntity();
        log.debug(productDto.toString());
        MultipartFile file = productDto.getFile();
        log.debug("File is empty: " + file.isEmpty());

        if (file != null && !file.isEmpty()) {
            try {
                String imageUrl = minioService.uploadFile(file);
                product.setImageUrl(imageUrl);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Product createdProduct = productService.createProduct(product);

        // creating default colored product
        coloredProductService
                .createColoredProduct(ColoredProduct.builder()
                        .name("default")
                        .imageUrl(createdProduct.getImageUrl())
                        .product(createdProduct)
                        .build());

        return new ResponseEntity<>(
                ProductDto.fromEntity(createdProduct),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDto productDto) throws Exception {
        Product product = productDto.toEntity();
        MultipartFile file = productDto.getFile();

        try {

            if (file != null && !file.isEmpty()) {
                try {
                    log.debug(file.toString());
                    String imageUrl = minioService.uploadFile(file);
                    product.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

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
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
