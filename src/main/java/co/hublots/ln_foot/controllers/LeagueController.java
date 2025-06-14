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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.services.LeagueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/leagues")
@RequiredArgsConstructor
public class LeagueController {
    private final LeagueService leagueService;

    @GetMapping
    public ResponseEntity<Page<LeagueDto>> listLeagues( // Changed return type
            @RequestParam(required = false) @Size(max = 100, message = "Country parameter is too long") String country,
            @RequestParam(required = false) @Size(max = 50, message = "Type parameter is too long") String type,
            Pageable pageable) {
        Page<LeagueDto> leaguePage = leagueService.listLeagues(country, type, pageable);
        return ResponseEntity.ok(leaguePage);
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
        LeagueDto createdLeague = leagueService.createLeague(createDto);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDto> updateLeague(@PathVariable String id,
            @Valid @RequestBody UpdateLeagueDto updateDto) {
        LeagueDto updatedLeague = leagueService.updateLeague(id, updateDto);
        return ResponseEntity.ok(updatedLeague);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeague(@PathVariable String id) {
        leagueService.deleteLeague(id);
        return ResponseEntity.noContent().build();
    }
}
