package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.SizeDto;
import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.services.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sizes")
public class SizeController {

    private final SizeService sizeService;

    @GetMapping
    public List<SizeDto> getAllSizes() {
        List<Size> sizes = sizeService.getAllSizes();
        return sizes.stream()
                .map(SizeDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeDto> getSizeById(@PathVariable Long id) {
        try {
            Size size = sizeService.getSizeById(id);
            return new ResponseEntity<>(SizeDto.from(size), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<SizeDto> createSize(@Valid @RequestBody SizeDto sizeDto) {
        Size size = Size.builder()
                .name(sizeDto.getName())
                .build();
        Size createdSize = sizeService.createSize(size);
        return new ResponseEntity<>(SizeDto.from(createdSize), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeDto> updateSize(@PathVariable Long id, @Valid @RequestBody SizeDto sizeDto) {
        try {
            Size size = Size.builder()
                    .name(sizeDto.getName())
                    .build();
            Size updatedSize = sizeService.updateSize(id, size);
            return new ResponseEntity<>(SizeDto.from(updatedSize), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSize(@PathVariable Long id) {
        try {
            sizeService.deleteSize(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
} 