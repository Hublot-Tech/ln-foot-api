package co.hublots.ln_foot.controllers;

import jakarta.validation.Valid; // Added import
import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank; // Added
import org.springframework.validation.annotation.Validated; // Added
import org.springframework.data.domain.Page; // Added
import org.springframework.data.domain.Pageable; // Added
import lombok.extern.slf4j.Slf4j; // Added
import jakarta.persistence.EntityNotFoundException; // Added for future try-catch if service throws it for fixtureId
import java.util.Map; // For error response

import java.util.List; // Keep if other methods return List

@Slf4j // Added
@Validated // Added
@RestController
@RequestMapping("/api/v1/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    public HighlightController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @GetMapping
    public ResponseEntity<?> listHighlightsByFixture( // Renamed method, changed return type
            @RequestParam @NotBlank(message = "fixtureApiId is required.") String fixtureApiId, // Made mandatory & validated
            Pageable pageable) {
        try {
            Page<HighlightDto> highlights = highlightService.listHighlightsByFixture(fixtureApiId, pageable);
            return ResponseEntity.ok(highlights);
        } catch (IllegalArgumentException e) { // From service if fixtureApiId is blank
            log.warn("Invalid argument for listing highlights: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) { // If service throws this for fixtureApiId not found
            log.warn("Fixture not found when listing highlights for fixtureApiId {}: {}", fixtureApiId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HighlightDto> findHighlightById(@PathVariable String id) {
        return highlightService.findHighlightById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("hasRole('ADMIN') or hasPermission(#createDto.fixtureId, 'FIXTURE', 'EDIT_HIGHLIGHTS')") // Example more granular permission

    // Removed comment as import is now at top

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> createHighlight(@Valid @RequestBody CreateHighlightDto createDto) {
        // Assuming service might throw EntityNotFoundException if referenced fixtureId in createDto is invalid
        // Or IllegalArgumentException for other issues not caught by @Valid
        // For now, relying on @Valid for DTO level and basic service exceptions.
        HighlightDto createdHighlight = highlightService.createHighlight(createDto);
        return new ResponseEntity<>(createdHighlight, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> updateHighlight(@PathVariable String id, @Valid @RequestBody UpdateHighlightDto updateDto) {
        try {
            HighlightDto updatedHighlight = highlightService.updateHighlight(id, updateDto);
            return ResponseEntity.ok(updatedHighlight);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent Highlight with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
        // Add other relevant exception catches if needed, e.g., DataAccessException
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHighlight(@PathVariable String id) {
        highlightService.deleteHighlight(id);
        return ResponseEntity.noContent().build();
    }
}
