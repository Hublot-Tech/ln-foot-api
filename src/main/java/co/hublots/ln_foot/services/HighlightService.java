package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;

import java.util.List;
import java.util.Optional;

public interface HighlightService {
    List<HighlightDto> listHighlights(String fixtureId);
    Optional<HighlightDto> findHighlightById(String id);
    HighlightDto createHighlight(CreateHighlightDto createDto);
    HighlightDto updateHighlight(String id, UpdateHighlightDto updateDto);
    void deleteHighlight(String id);
}
