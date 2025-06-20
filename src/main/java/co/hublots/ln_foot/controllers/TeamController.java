package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.services.TeamService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize; // If needed for read ops

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // Example: /api/v1/teams?leagueId=XYZ
    @GetMapping
    public List<TeamDto> listTeams(
            @RequestParam Optional<String> leagueId) {
        return teamService.listTeams(leagueId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> findTeamById(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return teamService.findTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
