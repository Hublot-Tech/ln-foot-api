package co.hublots.ln_foot.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    @GetMapping
    public ResponseEntity<Page<HighlightDto>> listHighlights(Pageable pageable) {
        Page<HighlightDto> highlights = highlightService.listHighlights(pageable);
        return ResponseEntity.ok(highlights);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HighlightDto> findHighlightById(@PathVariable String id) {
        return highlightService.findHighlightById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> createHighlight(@Valid @RequestBody CreateHighlightDto createDto) {
        HighlightDto createdHighlight = highlightService.createHighlight(createDto);
        return new ResponseEntity<>(createdHighlight, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> updateHighlight(@PathVariable String id,
            @Valid @RequestBody UpdateHighlightDto updateDto) {
        HighlightDto updatedHighlight = highlightService.updateHighlight(id, updateDto);
        return ResponseEntity.ok(updatedHighlight);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHighlight(@PathVariable String id) {
        highlightService.deleteHighlight(id);
        return ResponseEntity.noContent().build();
    }
}
