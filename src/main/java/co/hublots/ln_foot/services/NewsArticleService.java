package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;

import java.util.List;
import java.util.Optional;

public interface NewsArticleService {
    List<NewsArticleDto> listNewsArticles(String status, List<String> tags);
    Optional<NewsArticleDto> findNewsArticleById(String id);
    NewsArticleDto createNewsArticle(CreateNewsArticleDto createDto);
    NewsArticleDto updateNewsArticle(String id, UpdateNewsArticleDto updateDto);
    void deleteNewsArticle(String id);
}
