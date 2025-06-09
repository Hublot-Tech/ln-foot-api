package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.TeamDto;
import co.hublots.ln_foot.services.TeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import org.springframework.security.access.prepost.PreAuthorize; // If needed for read ops

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // Example: /api/v1/teams?leagueId=XYZ&season=2023
    @GetMapping
    public List<TeamDto> listTeamsByLeague(
            @RequestParam String leagueId, // Assuming leagueId is mandatory for listing teams
            @RequestParam(required = false) String season) {
        return teamService.listTeamsByLeague(leagueId, season);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> findTeamById(@PathVariable String id) {
        return teamService.findTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
