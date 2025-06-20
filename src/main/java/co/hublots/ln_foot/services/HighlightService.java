package co.hublots.ln_foot.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;

public interface HighlightService {
    Page<HighlightDto> listHighlights(Pageable pageable);
    Optional<HighlightDto> findHighlightById(String id);
    HighlightDto createHighlight(CreateHighlightDto createDto);
    HighlightDto updateHighlight(String id, UpdateHighlightDto updateDto);
    void deleteHighlight(String id);
}
