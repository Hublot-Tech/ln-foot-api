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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.SizeDto;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.services.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sizes")
public class SizeController {

    private final SizeService sizeService;

    @GetMapping
    public List<SizeDto> getAllSizes() {
        List<Size> sizes = sizeService.getAllSizes();
        return sizes
                .stream()
                .map(SizeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeDto> getSizeById(@PathVariable String id) {
        Size size = sizeService.getSizeById(id);
        return new ResponseEntity<>(
                SizeDto.fromEntity(size),
                HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SizeDto> createSize(
            @Valid @RequestBody SizeDto sizeDto) {
        Size size = sizeDto.toEntity();
        Size createdSize = sizeService.createSize(size);
        return new ResponseEntity<>(
                SizeDto.fromEntity(createdSize),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SizeDto> updateSize(
            @PathVariable String id,
            @Valid @RequestBody SizeDto sizeDto) {
        Size size = sizeDto.toEntity();
        Size updatedSize = sizeService.updateSize(id, size);
        return new ResponseEntity<>(
                SizeDto.fromEntity(updatedSize),
                HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSize(@PathVariable String id) {
        sizeService.deleteSize(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
