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

import co.hublots.ln_foot.dto.ColorDto;
import co.hublots.ln_foot.models.Color;
import co.hublots.ln_foot.services.ColorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/colors")
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public List<ColorDto> getAllColors() {
        List<Color> colors = colorService.getAllColors();
        return colors.stream()
                .map(ColorDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorDto> getColorById(@PathVariable UUID id) {
        try {
            Color color = colorService.getColorById(id);
            return new ResponseEntity<>(ColorDto.fromEntity(color), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ColorDto> createColor(@Valid @RequestBody ColorDto colorDto) {
        Color color = colorDto.toEntity();
        Color createdColor = colorService.createColor(color);
        return new ResponseEntity<>(ColorDto.fromEntity(createdColor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<ColorDto> updateColor(@PathVariable UUID id, @Valid @RequestBody ColorDto colorDto) {
        try {
            Color color = colorDto.toEntity();
            Color updatedColor = colorService.updateColor(id, color);
            return new ResponseEntity<>(ColorDto.fromEntity(updatedColor), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = { @SecurityRequirement(name = "bearerAuth") })
    public ResponseEntity<Void> deleteColor(@PathVariable UUID id) {
        try {
            colorService.deleteColor(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}