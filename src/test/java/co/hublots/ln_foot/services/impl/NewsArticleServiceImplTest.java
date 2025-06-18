package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.models.NewsArticle;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.repositories.NewsArticleRepository;

@ExtendWith(MockitoExtension.class)
class NewsArticleServiceImplTest {

    @MockitoBean
    private NewsArticleRepository newsArticleRepository;

    private NewsArticleServiceImpl newsArticleService;

    @BeforeEach
    void setUp() {
        newsArticleService = new NewsArticleServiceImpl(newsArticleRepository);
    }

    private User createMockUser(String id, String firstName, String lastName) {
        return User.builder().id(id).firstName(firstName).lastName(lastName).email(firstName + "@test.com")
                .role("EDITOR").build();
    }

    private NewsArticle createMockNewsArticle(String id, String title, String authorName) {
        return NewsArticle.builder()
                .id(id)
                .title(title)
                .content("Some content for " + title)
                .authorName(authorName) // Text fallback if author entity not linked
                .publicationDate(LocalDateTime.now().minusDays(1))
                .sourceUrl("http://source.url/" + title.toLowerCase().replace(" ", "-"))
                .imageUrl("http://image.url/" + title.toLowerCase().replace(" ", "-") + ".jpg")
                .status("published")
                .category("General")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusHours(12))
                .build();
    }

    @Test
    void listNewsArticles_noStatus_returnsAllSorted() {
        // Arrange
        NewsArticle article1 = createMockNewsArticle("1", "Article Alpha", "Alpha Author");
        NewsArticle article2 = createMockNewsArticle("2", "Article Beta", "Beta Author");
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "publicationDate");
        when(newsArticleRepository.findAll(defaultSort)).thenReturn(List.of(article1, article2));

        // Act
        List<NewsArticleDto> result = newsArticleService.listNewsArticles(null); // status is null

        // Assert
        assertEquals(2, result.size());
        verify(newsArticleRepository).findAll(defaultSort);
    }

    @Test
    void listNewsArticles_withStatus_returnsFilteredAndSorted() {
        // Arrange
        String status = "published";
        NewsArticle article1 = createMockNewsArticle("1", "Published Article", "Pub Author");
        article1.setStatus(status);
        when(newsArticleRepository.findByStatusOrderByPublicationDateDesc(status)).thenReturn(List.of(article1));

        // Act
        List<NewsArticleDto> result = newsArticleService.listNewsArticles(status);

        // Assert
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        verify(newsArticleRepository).findByStatusOrderByPublicationDateDesc(status);
    }

    @Test
    void findNewsArticleById_whenFound_mapsAuthorCorrectly() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        User authorUser = createMockUser(UUID.randomUUID().toString(), "John", "Doe");
        NewsArticle mockArticle = createMockNewsArticle(articleId, "Test Article with Author Entity",
                authorUser.getFirstName() + " " + authorUser.getLastName());

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));

        // Act
        Optional<NewsArticleDto> result = newsArticleService.findNewsArticleById(articleId);

        // Assert
        assertTrue(result.isPresent());
        NewsArticleDto dto = result.get();
        assertEquals(articleId, dto.getId());
        assertNotNull(dto.getAuthorName());
        assertEquals("John Doe", dto.getAuthorName());
    }

    @Test
    void findNewsArticleById_whenFound_mapsAuthorNameCorrectly_ifAuthorEntityNull() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        NewsArticle mockArticle = createMockNewsArticle(articleId, "Test Article with Author Name", "Jane Doe (text)");

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));

        // Act
        Optional<NewsArticleDto> result = newsArticleService.findNewsArticleById(articleId);

        // Assert
        assertTrue(result.isPresent());
        NewsArticleDto dto = result.get();
        assertEquals("Jane Doe (text)", dto.getAuthorName());
    }

    @Test
    void createNewsArticle_withAuthorId_linksAuthorAndSaves() {
        // Arrange
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Article by ID")
                .content("Content...")
                .authorName("Test Author")
                .publishedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .status("draft")
                .build();

        ArgumentCaptor<NewsArticle> articleCaptor = ArgumentCaptor.forClass(NewsArticle.class);
        when(newsArticleRepository.save(articleCaptor.capture())).thenAnswer(invocation -> {
            NewsArticle savedArticle = invocation.getArgument(0);
            savedArticle.setId(UUID.randomUUID().toString());
            savedArticle.setCreatedAt(LocalDateTime.now());
            savedArticle.setUpdatedAt(LocalDateTime.now());
            return savedArticle;
        });

        // Act
        NewsArticleDto resultDto = newsArticleService.createNewsArticle(createDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals("Test Author", resultDto.getAuthorName());

        NewsArticle captured = articleCaptor.getValue();
        assertEquals("Test Author", captured.getAuthorName());

        verify(newsArticleRepository).save(any(NewsArticle.class));
    }

    @Test
    void createNewsArticle_withoutAuthorId_savesWithNullAuthor() {
        // Arrange
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Article no specific author ID")
                .content("Content...")
                .publishedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .status("draft")
                .build();

        ArgumentCaptor<NewsArticle> articleCaptor = ArgumentCaptor.forClass(NewsArticle.class);
        when(newsArticleRepository.save(articleCaptor.capture())).thenAnswer(invocation -> {
            NewsArticle savedArticle = invocation.getArgument(0);
            savedArticle.setId(UUID.randomUUID().toString());
            return savedArticle;
        });

        // Act
        NewsArticleDto resultDto = newsArticleService.createNewsArticle(createDto);

        // Assert
        assertNotNull(resultDto);
        assertNull(resultDto.getAuthorName()); // UserDto author should be null

        NewsArticle captured = articleCaptor.getValue();
        assertNull(captured.getAuthorName()); // User entity link should be null

        verify(newsArticleRepository).save(any(NewsArticle.class));
    }

    @Test
    void updateNewsArticle_whenFound_updatesAndReturnsDto() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        User author = createMockUser(UUID.randomUUID().toString(), "Old", "Author");
        NewsArticle existingArticle = createMockNewsArticle(articleId, "Old Title",
                author.getFirstName() + " " + author.getLastName());

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));

        String newAuthorId = UUID.randomUUID().toString();
        User newAuthor = createMockUser(newAuthorId, "New", "Scribe");

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));

        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder()
                .title("Updated Title")
                .build();

        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        NewsArticleDto resultDto = newsArticleService.updateNewsArticle(articleId, updateDto);

        // Assert
        assertEquals("Updated Title", resultDto.getTitle());
        assertNotNull(resultDto.getAuthorName());
        assertEquals("New Scribe", resultDto.getAuthorName());

        verify(newsArticleRepository).findById(articleId);
        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(newsArticleRepository).save(captor.capture());
        assertEquals(newAuthor.getFirstName() + " " + newAuthor.getLastName(), captor.getValue().getAuthorName());
    }

    @Test
    void updateNewsArticle_setAuthorToNull_ifAuthorIdIsBlank() {
        String articleId = UUID.randomUUID().toString();
        User author = createMockUser(UUID.randomUUID().toString(), "Old", "Author");
        NewsArticle existingArticle = createMockNewsArticle(articleId, "Old Title",
                author.getFirstName() + " " + author.getLastName());

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder().build(); // Blank authorId

        NewsArticleDto resultDto = newsArticleService.updateNewsArticle(articleId, updateDto);
        assertNull(resultDto.getAuthorName());

        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(newsArticleRepository).save(captor.capture());
        assertNull(captor.getValue().getAuthorName());
    }

    @Test
    void deleteNewsArticle_whenFound_deletesArticle() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        when(newsArticleRepository.existsById(articleId)).thenReturn(true);
        doNothing().when(newsArticleRepository).deleteById(articleId);

        // Act
        assertDoesNotThrow(() -> newsArticleService.deleteNewsArticle(articleId));

        // Assert
        verify(newsArticleRepository).existsById(articleId);
        verify(newsArticleRepository).deleteById(articleId);
    }
}
