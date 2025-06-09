package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    public HighlightController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @GetMapping
    public List<HighlightDto> listHighlights(@RequestParam(required = false) String fixtureId) {
        // If fixtureId is provided, the service should filter by it.
        // For now, service mock returns all or empty.
        return highlightService.listHighlights(fixtureId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HighlightDto> findHighlightById(@PathVariable String id) {
        return highlightService.findHighlightById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // @PreAuthorize("hasRole('ADMIN') or hasPermission(#createDto.fixtureId, 'FIXTURE', 'EDIT_HIGHLIGHTS')") // Example more granular permission
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> createHighlight(@RequestBody CreateHighlightDto createDto) {
        HighlightDto createdHighlight = highlightService.createHighlight(createDto);
        return new ResponseEntity<>(createdHighlight, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HighlightDto> updateHighlight(@PathVariable String id, @RequestBody UpdateHighlightDto updateDto) {
        HighlightDto updatedHighlight = highlightService.updateHighlight(id, updateDto);
        if (updatedHighlight != null) {
            return ResponseEntity.ok(updatedHighlight);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHighlight(@PathVariable String id) {
        highlightService.deleteHighlight(id);
        return ResponseEntity.noContent().build();
    }
}
