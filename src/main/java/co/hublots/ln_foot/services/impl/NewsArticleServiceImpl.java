package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.services.NewsArticleService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NewsArticleServiceImpl implements NewsArticleService {

    @Override
    public List<NewsArticleDto> listNewsArticles(String status, List<String> tags) {
        // Mock implementation
        return Collections.emptyList();
    }

    @Override
    public Optional<NewsArticleDto> findNewsArticleById(String id) {
        return Optional.empty();
    }

    @Override
    public NewsArticleDto createNewsArticle(CreateNewsArticleDto createDto) {
        return NewsArticleDto.builder()
                .id(UUID.randomUUID().toString())
                .title(createDto.getTitle())
                .content(createDto.getContent())
                .author(createDto.getAuthor())
                .source(createDto.getSource())
                .url(createDto.getUrl())
                .imageUrl(createDto.getImageUrl())
                .publishedAt(createDto.getPublishedAt() != null ? createDto.getPublishedAt() : OffsetDateTime.now())
                .tags(createDto.getTags() != null ? createDto.getTags() : Collections.emptyList())
                .status(createDto.getStatus() != null ? createDto.getStatus() : "draft")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public NewsArticleDto updateNewsArticle(String id, UpdateNewsArticleDto updateDto) {
        // Assume fetch, then update
        return NewsArticleDto.builder()
                .id(id)
                .title(updateDto.getTitle() != null ? updateDto.getTitle() : "Original Title")
                .content(updateDto.getContent() != null ? updateDto.getContent() : "Original Content")
                .author(updateDto.getAuthor() != null ? updateDto.getAuthor() : "Original Author")
                .source(updateDto.getSource() != null ? updateDto.getSource() : "Original Source")
                .url(updateDto.getUrl() != null ? updateDto.getUrl() : "http://original.url/article")
                .imageUrl(updateDto.getImageUrl() != null ? updateDto.getImageUrl() : "http://original.image/article.png")
                .publishedAt(updateDto.getPublishedAt()) // Keep original if not provided or handle logic
                .tags(updateDto.getTags())
                .status(updateDto.getStatus() != null ? updateDto.getStatus() : "published")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Override
    public void deleteNewsArticle(String id) {
        System.out.println("Deleting news article with id: " + id);
    }
}
