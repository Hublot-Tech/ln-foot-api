package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.models.NewsArticle;
import co.hublots.ln_foot.models.NewsArticle.NewsCategory;
import co.hublots.ln_foot.models.NewsArticle.NewsStatus;
import co.hublots.ln_foot.repositories.NewsArticleRepository;
import co.hublots.ln_foot.services.NewsArticleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsArticleServiceImpl implements NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;

    private NewsArticleDto mapToDto(NewsArticle entity) {
        if (entity == null) {
            return null;
        }
        return NewsArticleDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .authorName(entity.getAuthorName())
                .sourceUrl(entity.getSourceUrl())
                .imageUrl(entity.getImageUrl())
                .publishedAt(entity.getPublicationDate() != null ? entity.getPublicationDate().atOffset(ZoneOffset.UTC)
                        : null)
                .status(entity.getStatus())
                .tags(Collections.emptyList())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateNewsArticleDto dto, NewsArticle entity) {
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setAuthorName(dto.getAuthorName());
        entity.setSourceUrl(dto.getSourceUrl());
        entity.setImageUrl(dto.getImageUrl());
        entity.setSummary(dto.getSummary());
        entity.setPublicationDate(dto.getPublishedAt() != null ? dto.getPublishedAt().toLocalDateTime() : null);
        entity.setStatus(dto.getStatus());
        entity.setCategory(NewsCategory.GENERAL);
        entity.setTags(List.of());
    }

    private void mapToEntityForUpdate(UpdateNewsArticleDto dto, NewsArticle entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }

        if (dto.getSourceUrl() != null) {
            entity.setSourceUrl(dto.getSourceUrl());
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
        if (dto.getPublishedAt() != null) {
            entity.setPublicationDate(dto.getPublishedAt().toLocalDateTime());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        if (dto.getSummary() != null) {
            entity.setSummary(dto.getSummary());
        }
        if (dto.getTags() != null) {
            entity.setTags(dto.getTags());
        }
        if (dto.getIsMajorUpdate() != null) {
            entity.setIsMajorUpdate(dto.getIsMajorUpdate());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleDto> listNewsArticles(Optional<NewsStatus> status) {
        List<NewsArticle> articles;
        if (status.isPresent()) {
            articles = newsArticleRepository.findByStatusOrderByPublicationDateDesc(status.get());
        } else {
            // Default sort if no status specified
            articles = newsArticleRepository.findAll(Sort.by(Sort.Direction.DESC, "publicationDate"));
        }
        return articles.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NewsArticleDto> findNewsArticleById(String id) {
        return newsArticleRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public NewsArticleDto createNewsArticle(CreateNewsArticleDto createDto) {
        NewsArticle article = new NewsArticle();
        mapToEntityForCreate(createDto, article);
        NewsArticle savedArticle = newsArticleRepository.save(article);
        return mapToDto(savedArticle);
    }

    @Override
    @Transactional
    public NewsArticleDto updateNewsArticle(String id, UpdateNewsArticleDto updateDto) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NewsArticle with ID " + id + " not found"));
        mapToEntityForUpdate(updateDto, article);
        NewsArticle updatedArticle = newsArticleRepository.save(article);
        return mapToDto(updatedArticle);
    }

    @Override
    @Transactional
    public void deleteNewsArticle(String id) {
        if (!newsArticleRepository.existsById(id)) {
            throw new EntityNotFoundException("NewsArticle with ID " + id + " not found");
        }
        newsArticleRepository.deleteById(id);
    }
}