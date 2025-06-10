package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.services.NewsArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size; // For @Size
import org.springframework.validation.annotation.Validated; // For class-level validation
import lombok.extern.slf4j.Slf4j; // For logging
import jakarta.persistence.EntityNotFoundException; // For try-catch
import org.springframework.dao.DataAccessException; // For try-catch
import org.springframework.http.HttpStatus; // For explicit status codes

import java.util.List;

@Slf4j // Added
@Validated // Added for request param validation
@RestController
@RequestMapping("/api/v1/news-articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public List<NewsArticleDto> listNewsArticles(
            @RequestParam(required = false) @Size(max = 20, message = "Status parameter is too long") String status,
            @RequestParam(required = false) @Size(max = 5, message = "Cannot request more than 5 tags at once") List<@Size(max = 50, message = "Tag is too long") String> tags) {
        // Individual tag size validation List<@Size(max=50) String> tags
        return newsArticleService.listNewsArticles(status, tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> findNewsArticleById(@PathVariable String id) {
        try {
            // Assuming NewsArticleServiceImpl.findNewsArticleById might throw IllegalArgumentException
            // if id format is grossly invalid before DB lookup, though this is not standard for findById.
            // The primary purpose here is to handle EntityNotFoundException if the service threw it,
            // but it returns Optional, so current orElse is fine for not found.
            // Adding try-catch for IllegalArgumentException as per feedback suggestion.
            return newsArticleService.findNewsArticleById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("NewsArticle not found with ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ID format for NewsArticle: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> createNewsArticle(@Valid @RequestBody CreateNewsArticleDto createDto) {
        // Assuming service.createNewsArticle might throw exceptions for invalid data not caught by @Valid
        // (e.g., non-existent authorId if service checks it and throws EntityNotFoundException)
        // For now, relying on @Valid for request body and default Spring exception handling.
        // If service throws EntityNotFoundException for authorId, a @ControllerAdvice would be better.
        NewsArticleDto createdArticle = newsArticleService.createNewsArticle(createDto);
        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> updateNewsArticle(@PathVariable String id, @Valid @RequestBody UpdateNewsArticleDto updateDto) {
        try {
            NewsArticleDto updatedArticle = newsArticleService.updateNewsArticle(id, updateDto);
            return ResponseEntity.ok(updatedArticle);
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to update non-existent NewsArticle with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) { // Example for database errors
            log.error("Database error while updating NewsArticle with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        // Add other specific exceptions if needed
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable String id) {
        try {
            newsArticleService.deleteNewsArticle(id); // Service throws EntityNotFoundException if not found
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("Attempted to delete non-existent NewsArticle with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            log.error("Database error while deleting NewsArticle with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
