package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NewsArticleServiceImplTest {
    private NewsArticleServiceImpl newsArticleService;

    @BeforeEach
    void setUp() {
        newsArticleService = new NewsArticleServiceImpl();
    }

    @Test
    void listNewsArticles_returnsEmptyList() {
        List<NewsArticleDto> result = newsArticleService.listNewsArticles("published", Collections.singletonList("transfer"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findNewsArticleById_returnsEmptyOptional() {
        Optional<NewsArticleDto> result = newsArticleService.findNewsArticleById("article1");
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void createNewsArticle_returnsMockDto() {
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Big News Story")
                .content("Something happened...")
                .author("Journalist Joe")
                .status("draft")
                .tags(Collections.singletonList("breaking"))
                .build();
        NewsArticleDto result = newsArticleService.createNewsArticle(createDto);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Big News Story", result.getTitle());
        assertEquals("draft", result.getStatus());
        assertFalse(result.getTags().isEmpty());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateNewsArticle_returnsMockDto() {
        String articleId = "articleToUpdate";
        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder()
                .title("Updated News Story")
                .status("published")
                .build();
        NewsArticleDto result = newsArticleService.updateNewsArticle(articleId, updateDto);
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals("Updated News Story", result.getTitle());
        assertEquals("published", result.getStatus());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateNewsArticle_usesOriginalValues_whenUpdateDtoFieldsAreNull() {
        String articleId = "article-xyz";
        UpdateNewsArticleDto updateDtoWithNulls = new UpdateNewsArticleDto();

        NewsArticleDto result = newsArticleService.updateNewsArticle(articleId, updateDtoWithNulls);
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        assertEquals("Original Title", result.getTitle()); // From mock
        assertEquals("published", result.getStatus()); // Mock default if DTO status is null
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void deleteNewsArticle_completesWithoutError() {
        assertDoesNotThrow(() -> newsArticleService.deleteNewsArticle("articleToDelete"));
    }
}
