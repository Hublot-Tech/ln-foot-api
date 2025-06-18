package co.hublots.ln_foot.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.CreateFixtureDto;
import co.hublots.ln_foot.dto.FixtureDto;
import co.hublots.ln_foot.dto.UpdateFixtureDto;
import co.hublots.ln_foot.services.FixtureService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixtures")
public class FixtureController {

    private final FixtureService fixtureService;

    @GetMapping
    public ResponseEntity<Page<FixtureDto>> listFixtures(
            @RequestParam(required = false) String leagueApiId,
            Pageable pageable) {
        Page<FixtureDto> fixturePage = fixtureService.listFixtures(leagueApiId, pageable);
        return ResponseEntity.ok(fixturePage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixtureDto> findFixtureById(@PathVariable String id) {
        return fixtureService.findFixtureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    public List<FixtureDto> getUpcomingFixtures(
            @RequestParam(defaultValue = "7") @Min(1) @Max(30) Integer days,
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FixtureDto> updateFixture(@PathVariable String id,
            @Valid @RequestBody UpdateFixtureDto updateDto) {
        FixtureDto updatedFixture = fixtureService.updateFixture(id, updateDto);
        return new ResponseEntity<>(updatedFixture, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFixture(@PathVariable String id) {
        fixtureService.deleteFixture(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
