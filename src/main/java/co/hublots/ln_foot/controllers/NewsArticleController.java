package co.hublots.ln_foot.controllers;

import java.util.List;

import org.springframework.dao.DataAccessException;
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

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.services.NewsArticleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/news-articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public List<NewsArticleDto> listNewsArticles(
            @RequestParam(required = false) @Size(max = 20, message = "Status parameter is too long") String status) {
        return newsArticleService.listNewsArticles(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> findNewsArticleById(@PathVariable String id) {
        return newsArticleService.findNewsArticleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("NewsArticle not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> createNewsArticle(@Valid @RequestBody CreateNewsArticleDto createDto) {
        NewsArticleDto createdArticle = newsArticleService.createNewsArticle(createDto);
        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> updateNewsArticle(@PathVariable String id,
            @Valid @RequestBody UpdateNewsArticleDto updateDto) {
        try {
            NewsArticleDto updatedArticle = newsArticleService.updateNewsArticle(id, updateDto);
            return ResponseEntity.ok(updatedArticle);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent NewsArticle with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            log.error("Database error while updating NewsArticle with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable String id) {
        newsArticleService.deleteNewsArticle(id);
        return ResponseEntity.noContent().build();
    }
}
