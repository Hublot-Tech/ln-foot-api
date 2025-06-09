package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.services.NewsArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news-articles")
public class NewsArticleController {

    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public List<NewsArticleDto> listNewsArticles(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> tags) {
        return newsArticleService.listNewsArticles(status, tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsArticleDto> findNewsArticleById(@PathVariable String id) {
        return newsArticleService.findNewsArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> createNewsArticle(@RequestBody CreateNewsArticleDto createDto) {
        NewsArticleDto createdArticle = newsArticleService.createNewsArticle(createDto);
        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<NewsArticleDto> updateNewsArticle(@PathVariable String id, @RequestBody UpdateNewsArticleDto updateDto) {
        NewsArticleDto updatedArticle = newsArticleService.updateNewsArticle(id, updateDto);
        if (updatedArticle != null) {
            return ResponseEntity.ok(updatedArticle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Void> deleteNewsArticle(@PathVariable String id) {
        newsArticleService.deleteNewsArticle(id);
        return ResponseEntity.noContent().build();
    }
}
