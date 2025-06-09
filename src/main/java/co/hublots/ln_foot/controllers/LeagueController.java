package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.services.LeagueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping
    public List<LeagueDto> listLeagues(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) String type) {
        return leagueService.listLeagues(country, season, type);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueDto> findLeagueById(@PathVariable String id) {
        return leagueService.findLeagueById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDto> createLeague(@RequestBody CreateLeagueDto createDto) {
        LeagueDto createdLeague = leagueService.createLeague(createDto);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDto> updateLeague(@PathVariable String id, @RequestBody UpdateLeagueDto updateDto) {
        LeagueDto updatedLeague = leagueService.updateLeague(id, updateDto);
        if (updatedLeague != null) {
            return ResponseEntity.ok(updatedLeague);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeague(@PathVariable String id) {
        leagueService.deleteLeague(id);
        return ResponseEntity.noContent().build();
    }
}
