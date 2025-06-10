package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.services.LeagueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern; // For season pattern
import jakarta.validation.constraints.Size;   // For string sizes
import org.springframework.validation.annotation.Validated; // For class-level validation
import lombok.extern.slf4j.Slf4j; // For logging
import jakarta.persistence.EntityNotFoundException; // For try-catch
import org.springframework.dao.DataIntegrityViolationException; // For try-catch
import org.springframework.dao.DataAccessException; // For try-catch
// HttpStatus is already imported via ResponseEntity

import java.util.List;

@Slf4j // Added
@Validated // Added
@RestController
@RequestMapping("/api/v1/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping
    public List<LeagueDto> listLeagues(
            @RequestParam(required = false) @Size(max = 100, message = "Country parameter is too long") String country,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}(-\\d{4})?$", message = "Season must be like YYYY or YYYY-YYYY") String season,
            @RequestParam(required = false) @Size(max = 50, message = "Type parameter is too long") String type) {
        return leagueService.listLeagues(country, season, type);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueDto> findLeagueById(@PathVariable String id) {
        try {
            return leagueService.findLeagueById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("League not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ID format for League: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDto> createLeague(@Valid @RequestBody CreateLeagueDto createDto) {
        // Assuming service layer handles potential exceptions like duplicate apiLeagueId
        LeagueDto createdLeague = leagueService.createLeague(createDto);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDto> updateLeague(@PathVariable String id, @Valid @RequestBody UpdateLeagueDto updateDto) {
        try {
            LeagueDto updatedLeague = leagueService.updateLeague(id, updateDto);
            return ResponseEntity.ok(updatedLeague);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent league with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) { // Catching broader DB errors
            log.error("Database error while updating league with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeague(@PathVariable String id) {
        try {
            leagueService.deleteLeague(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to delete non-existent league with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataIntegrityViolationException e) {
            log.error("Cannot delete league with ID {}: data integrity violation (e.g., existing fixtures).", id, e);
            // Using a generic message for client, specific error logged for server side.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (DataAccessException e) {
            log.error("Database error while deleting league with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
