package co.hublots.ln_foot.controllers;

import java.util.List;

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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news-articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

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
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<NewsArticleDto> createNewsArticle(@Valid @RequestBody CreateNewsArticleDto createDto) {
        NewsArticleDto createdArticle = newsArticleService.createNewsArticle(createDto);
        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<NewsArticleDto> updateNewsArticle(@PathVariable String id,
            @Valid @RequestBody UpdateNewsArticleDto updateDto) {
        NewsArticleDto updatedArticle = newsArticleService.updateNewsArticle(id, updateDto);
        return ResponseEntity.ok(updatedArticle);

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable String id) {
        newsArticleService.deleteNewsArticle(id);
        return ResponseEntity.noContent().build();
    }
}
