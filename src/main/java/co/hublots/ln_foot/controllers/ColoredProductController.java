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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.dto.ColoredProductDto;
import co.hublots.ln_foot.models.ColoredProduct;
import co.hublots.ln_foot.services.ColoredProductService;
import co.hublots.ln_foot.services.MinioService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/colored-products")
public class ColoredProductController {

    private final MinioService minioService;
    private final ColoredProductService coloredProductService;

    @GetMapping
    public ResponseEntity<List<ColoredProductDto>> getColoredProducts() {
        List<ColoredProduct> coloredProducts = coloredProductService.getAllColoredProducts();
        return new ResponseEntity<>(
                coloredProducts.stream().map(ColoredProductDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ColoredProductDto> getColoredProduct(@PathVariable String id) {
        ColoredProduct coloredProduct = coloredProductService.getColoredProductById(id);
        return new ResponseEntity<>(ColoredProductDto.fromEntity(coloredProduct), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColoredProductDto> createColoredProduct(
            @RequestBody @Valid ColoredProductDto coloredProductDto) {

        MultipartFile file = coloredProductDto.getFile();
        ColoredProduct coloredProduct = coloredProductDto.toEntity();

        if (file != null) {
            String imageUrl = minioService.uploadFile(file);
            coloredProduct.setImageUrl(imageUrl);
        }

        ColoredProduct createdColor = coloredProductService.createColoredProduct(coloredProduct);
        return new ResponseEntity<>(ColoredProductDto.fromEntity(createdColor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColoredProductDto> updateColoredProduct(@PathVariable String id,
            @RequestBody @Valid ColoredProductDto coloredProductDto) {
        try {
            ColoredProduct coloredProduct = coloredProductDto.toEntity();
            MultipartFile file = coloredProductDto.getFile();

            if (file != null) {
                String imageUrl = minioService.uploadFile(file);
                coloredProduct.setImageUrl(imageUrl);
            }

            ColoredProduct updatedColor = coloredProductService.updateColoredProduct(id, coloredProduct);
            return new ResponseEntity<>(ColoredProductDto.fromEntity(updatedColor), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteColoredProduct(@PathVariable String id) {
        coloredProductService.deleteColoredProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}