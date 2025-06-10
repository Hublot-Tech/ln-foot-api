package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.services.FixtureService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Added import
import jakarta.persistence.EntityNotFoundException; // Added import
import lombok.extern.slf4j.Slf4j; // Added import for logging

import java.time.LocalDate;
import java.util.List;

@Slf4j // Added for logging
@RestController
@RequestMapping("/api/v1/fixtures")
public class FixtureController {

    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    @GetMapping
    public List<FixtureDto> listFixtures(
            @RequestParam(required = false) String leagueId,
            @RequestParam(required = false) String season) {
        return fixtureService.listFixtures(leagueId, season);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixtureDto> findFixtureById(@PathVariable String id) {
        return fixtureService.findFixtureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    public List<FixtureDto> getUpcomingFixtures(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(required = false) String leagueId) {
        return fixtureService.getUpcomingFixtures(days, leagueId);
    }

    @GetMapping("/by-date")
    public List<FixtureDto> getFixturesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String leagueId) {
        return fixtureService.getFixturesByDate(date, leagueId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FixtureDto> createFixture(@Valid @RequestBody CreateFixtureDto createDto) {
        FixtureDto createdFixture = fixtureService.createFixture(createDto);
        return new ResponseEntity<>(createdFixture, HttpStatus.CREATED);
    }

    @PutMapping("/{id}") // Path variable should be apiFixtureId if that's what service expects
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FixtureDto> updateFixture(@PathVariable String id, @Valid @RequestBody UpdateFixtureDto updateDto) {
        try {
            FixtureDto updatedFixture = fixtureService.updateFixture(id, updateDto); // Service throws if not found
            return ResponseEntity.ok(updatedFixture);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent fixture with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}") // Path variable should be apiFixtureId
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFixture(@PathVariable String id) {
        try {
            fixtureService.deleteFixture(id); // Service throws EntityNotFoundException if not found
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to delete non-existent fixture with id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
