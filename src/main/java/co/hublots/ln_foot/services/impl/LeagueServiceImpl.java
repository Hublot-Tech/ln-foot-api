package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import co.hublots.ln_foot.dto.CreateLeagueDto;
import co.hublots.ln_foot.dto.LeagueDto;
import co.hublots.ln_foot.dto.UpdateLeagueDto;
import co.hublots.ln_foot.models.League;
import co.hublots.ln_foot.repositories.LeagueRepository;
import co.hublots.ln_foot.services.LeagueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeagueServiceImpl implements LeagueService {

    private final LeagueRepository leagueRepository;

    private LeagueDto mapToDto(League entity) {
        if (entity == null) {
            return null;
        }
        return LeagueDto.builder()
                .id(entity.getApiLeagueId())
                .name(entity.getLeagueName())
                .country(entity.getCountry())
                .logoUrl(entity.getLogoUrl())

                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateLeagueDto dto, League entity) {
        entity.setApiLeagueId(dto.getId());
        entity.setLeagueName(dto.getName());
        entity.setCountry(dto.getCountry());
        entity.setLogoUrl(dto.getLogoUrl());

    }

    private void mapToEntityForUpdate(UpdateLeagueDto dto, League entity) {
        if (dto.getName() != null) {
            entity.setLeagueName(dto.getName());
        }
        if (dto.getCountry() != null) {
            entity.setCountry(dto.getCountry());
        }
        if (dto.getLogoUrl() != null) {
            entity.setLogoUrl(dto.getLogoUrl());
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeagueDto> listLeagues(String country, String type, Pageable pageable) {
        log.debug("Listing leagues with country: [{}], type: [{}], pageable: {}", country, type, pageable);

        Specification<League> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(country)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("country"), country));
        }

        if (StringUtils.hasText(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("sportId"), type));
        }
        if (StringUtils.hasText(type)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("leagueTypeField"), type));
        }
        if (StringUtils.hasText(type)) {
            log.warn(
                    "Filtering by 'type' ('{}') is not fully implemented as League entity may not have a direct 'type' field. This filter might be ignored or adapted.",
                    type);

            spec = spec.and((root, query, cb) -> cb.equal(root.get("sportId"), type));
        }

        Page<League> leaguePage = leagueRepository.findAll(spec, pageable);
        log.debug("Found {} leagues matching criteria.", leaguePage.getTotalElements());
        return leaguePage.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LeagueDto> findLeagueById(String apiLeagueId) {
        return leagueRepository.findByApiLeagueId(apiLeagueId).map(this::mapToDto);
    }

    @Override
    @Transactional
    public LeagueDto createLeague(CreateLeagueDto createDto) {

        leagueRepository.findByApiLeagueId(createDto.getId()).ifPresent(l -> {
            throw new IllegalStateException("League with apiLeagueId " + createDto.getId() + " already exists.");
        });
        League league = new League();
        mapToEntityForCreate(createDto, league);

        League savedLeague = leagueRepository.save(league);
        return mapToDto(savedLeague);
    }

    @Override
    @Transactional
    public LeagueDto updateLeague(String apiLeagueId, UpdateLeagueDto updateDto) {
        League league = leagueRepository.findByApiLeagueId(apiLeagueId)
                .orElseThrow(
                        () -> new EntityNotFoundException("League with apiLeagueId " + apiLeagueId + " not found"));
        mapToEntityForUpdate(updateDto, league);
        League updatedLeague = leagueRepository.save(league);
        return mapToDto(updatedLeague);
    }

    @Override
    @Transactional
    public void deleteLeague(String apiLeagueId) {
        League league = leagueRepository.findByApiLeagueId(apiLeagueId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "League with apiLeagueId " + apiLeagueId + " not found before deletion"));
        leagueRepository.delete(league);

    }
}
