package co.hublots.ln_foot.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
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

    @GetMapping("/{id}")
    public ResponseEntity<ColoredProductDto> getColoredProduct(@PathVariable String id) {
        try {
            ColoredProduct coloredProduct = coloredProductService.getColoredProductById(id);
            return new ResponseEntity<>(ColoredProductDto.fromEntity(coloredProduct), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColoredProductDto> createColoredProduct(
            @RequestBody @Valid ColoredProductDto coloredProductDto) {

        MultipartFile file = coloredProductDto.getFile();
        ColoredProduct coloredProduct = coloredProductDto.toEntity();

        if (file != null) {
            try {
                String imageUrl = minioService.uploadFile(file);
                coloredProduct.setImageUrl(imageUrl);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
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
                try {
                    String imageUrl = minioService.uploadFile(file);
                    coloredProduct.setImageUrl(imageUrl);
                } catch (Exception e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            ColoredProduct updatedColor = coloredProductService.updateColoredProduct(id, coloredProduct);
            return new ResponseEntity<>(ColoredProductDto.fromEntity(updatedColor), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteColoredProduct(@PathVariable String id) {
        try {
            coloredProductService.deleteColoredProduct(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}