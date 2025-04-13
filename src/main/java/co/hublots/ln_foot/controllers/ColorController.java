package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.ColorDto;
import co.hublots.ln_foot.models.Color;
import co.hublots.ln_foot.services.ColorService;
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
    public ResponseEntity<ColorDto> getColorById(@PathVariable Long id) {
        try {
            Color color = colorService.getColorById(id);
            return new ResponseEntity<>(ColorDto.fromEntity(color), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<ColorDto> createColor(@Valid @RequestBody ColorDto colorDto) {
        Color color = colorDto.toEntity();
        Color createdColor = colorService.createColor(color);
        return new ResponseEntity<>(ColorDto.fromEntity(createdColor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorDto> updateColor(@PathVariable Long id, @Valid @RequestBody ColorDto colorDto) {
        try {
            Color color = colorDto.toEntity();
            Color updatedColor = colorService.updateColor(id, color);
            return new ResponseEntity<>(ColorDto.fromEntity(updatedColor), HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        try {
            colorService.deleteColor(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
} 