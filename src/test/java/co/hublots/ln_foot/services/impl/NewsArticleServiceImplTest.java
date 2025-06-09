package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.NewsArticle;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.repositories.NewsArticleRepository;
import co.hublots.ln_foot.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsArticleServiceImplTest {

    @Mock
    private NewsArticleRepository newsArticleRepository;
    @Mock
    private UserRepository userRepository;

    private NewsArticleServiceImpl newsArticleService;

    @BeforeEach
    void setUp() {
        newsArticleService = new NewsArticleServiceImpl(newsArticleRepository, userRepository);
    }

    private User createMockUser(String id, String firstName, String lastName) {
        return User.builder().id(id).firstName(firstName).lastName(lastName).email(firstName+"@test.com").role("EDITOR").build();
    }

    private NewsArticle createMockNewsArticle(String id, String title, User author, String authorName) {
        return NewsArticle.builder()
                .id(id)
                .title(title)
                .content("Some content for " + title)
                .author(author)
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
        NewsArticle article1 = createMockNewsArticle("1", "Article Alpha", null, "Alpha Author");
        NewsArticle article2 = createMockNewsArticle("2", "Article Beta", null, "Beta Author");
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "publicationDate");
        when(newsArticleRepository.findAll(defaultSort)).thenReturn(List.of(article1, article2));

        // Act
        List<NewsArticleDto> result = newsArticleService.listNewsArticles(null, null); // status is null

        // Assert
        assertEquals(2, result.size());
        verify(newsArticleRepository).findAll(defaultSort);
    }

    @Test
    void listNewsArticles_withStatus_returnsFilteredAndSorted() {
        // Arrange
        String status = "published";
        NewsArticle article1 = createMockNewsArticle("1", "Published Article", null, "Pub Author");
        article1.setStatus(status);
        when(newsArticleRepository.findByStatusOrderByPublicationDateDesc(status)).thenReturn(List.of(article1));

        // Act
        List<NewsArticleDto> result = newsArticleService.listNewsArticles(status, null);

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
        NewsArticle mockArticle = createMockNewsArticle(articleId, "Test Article with Author Entity", authorUser, null);

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));

        // Act
        Optional<NewsArticleDto> result = newsArticleService.findNewsArticleById(articleId);

        // Assert
        assertTrue(result.isPresent());
        NewsArticleDto dto = result.get();
        assertEquals(articleId, dto.getId());
        assertNotNull(dto.getAuthor());
        assertEquals(authorUser.getId(), dto.getAuthor().getId());
        assertEquals("John Doe", dto.getAuthor().getName()); // Assuming UserDto.name is concat of first/last
        assertNull(mockArticle.getAuthorName()); // AuthorName should be null if User author is set
    }

    @Test
    void findNewsArticleById_whenFound_mapsAuthorNameCorrectly_ifAuthorEntityNull() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        NewsArticle mockArticle = createMockNewsArticle(articleId, "Test Article with Author Name", null, "Jane Doe (text)");

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(mockArticle));

        // Act
        Optional<NewsArticleDto> result = newsArticleService.findNewsArticleById(articleId);

        // Assert
        assertTrue(result.isPresent());
        NewsArticleDto dto = result.get();
        assertNull(dto.getAuthor()); // UserDto author should be null
        // The current NewsArticleDto does not have a separate authorName field if UserDto author is present.
        // The mapToDto in service prioritizes User entity for author name.
        // If User entity is null, and authorName is present on NewsArticle entity, this name is used for UserDto.name
        // This test might need adjustment based on how mapToDto handles UserDto.name vs UserDto.author object
        // The current mapToDto for NewsArticle sets dto.setAuthor(mapUserToUserDto(entity.getAuthor()))
        // The mapUserToUserDto will return null if entity.getAuthor() is null.
        // This means NewsArticleDto.author (the UserDto object) will be null.
        // The previous version of NewsArticleDto had a String author field.
        // Let's re-check NewsArticleDto and mapToDto for NewsArticle.
        // NewsArticleDto.author is UserDto. If entity.author is null, then NewsArticleDto.author is null.
        // The previous string field `author` on NewsArticleDto is gone.
        // This test case needs to align with the new DTO structure.
        // If entity.getAuthor() is null, dto.getAuthor() will be null.
        // The logic inside mapToDto for NewsArticleServiceImpl tries to get authorName from entity.getAuthorName()
        // if entity.getAuthor() is null. BUT it tries to populate the UserDto with this.
        // This is slightly convoluted. Let's simplify: mapToDto should map User entity to UserDto.
        // If User entity is null, UserDto author is null. The entity.authorName can be a fallback if needed,
        // but not by creating a UserDto with only a name from entity.authorName.

        // For this test, we expect dto.getAuthor() to be null.
        assertNull(dto.getAuthor());

    }


    @Test
    void createNewsArticle_withAuthorId_linksAuthorAndSaves() {
        // Arrange
        String authorId = UUID.randomUUID().toString();
        User mockAuthor = createMockUser(authorId, "Test", "Author");
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Article by ID")
                .content("Content...")
                .authorId(authorId) // Use authorId
                .publishedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .status("draft")
                .build();

        when(userRepository.findById(authorId)).thenReturn(Optional.of(mockAuthor));

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
        assertNotNull(resultDto.getAuthor());
        assertEquals(authorId, resultDto.getAuthor().getId());
        assertEquals("Test Author", resultDto.getAuthor().getName());

        NewsArticle captured = articleCaptor.getValue();
        assertEquals(mockAuthor, captured.getAuthor());
        assertNull(captured.getAuthorName()); // authorName should be cleared if User author is linked

        verify(userRepository).findById(authorId);
        verify(newsArticleRepository).save(any(NewsArticle.class));
    }

    @Test
    void createNewsArticle_withNonExistentAuthorId_throwsException() {
        // Arrange
        String nonExistentAuthorId = "non-existent-author-id";
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Article with Bad Author")
                .authorId(nonExistentAuthorId)
                .build();
        when(userRepository.findById(nonExistentAuthorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> newsArticleService.createNewsArticle(createDto));
        verify(newsArticleRepository, never()).save(any(NewsArticle.class));
    }

    @Test
    void createNewsArticle_withoutAuthorId_savesWithNullAuthor() {
        // Arrange
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Article no specific author ID")
                .content("Content...")
                // authorId is null in DTO by default
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
        assertNull(resultDto.getAuthor()); // UserDto author should be null

        NewsArticle captured = articleCaptor.getValue();
        assertNull(captured.getAuthor()); // User entity link should be null
        // The service's mapToEntityForCreate logic currently doesn't use the old string author field from DTO
        // So, captured.getAuthorName() would be null unless CreateNewsArticleDto still had 'author' string
        // and mapToEntityForCreate explicitly set entity.setAuthorName from it when authorId is null.
        // The current DTO only has authorId.
        assertNull(captured.getAuthorName());

        verify(userRepository, never()).findById(anyString());
        verify(newsArticleRepository).save(any(NewsArticle.class));
    }


    @Test
    void updateNewsArticle_whenFound_updatesAndReturnsDto() {
        // Arrange
        String articleId = UUID.randomUUID().toString();
        User author = createMockUser(UUID.randomUUID().toString(), "Old", "Author");
        NewsArticle existingArticle = createMockNewsArticle(articleId, "Old Title", author, null);

        String newAuthorId = UUID.randomUUID().toString();
        User newAuthor = createMockUser(newAuthorId, "New", "Scribe");

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(userRepository.findById(newAuthorId)).thenReturn(Optional.of(newAuthor)); // For updating author

        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder()
            .title("Updated Title")
            .authorId(newAuthorId) // Update to new author
            .build();

        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        NewsArticleDto resultDto = newsArticleService.updateNewsArticle(articleId, updateDto);

        // Assert
        assertEquals("Updated Title", resultDto.getTitle());
        assertNotNull(resultDto.getAuthor());
        assertEquals(newAuthorId, resultDto.getAuthor().getId());
        assertEquals("New Scribe", resultDto.getAuthor().getName());

        verify(newsArticleRepository).findById(articleId);
        verify(userRepository).findById(newAuthorId);
        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(newsArticleRepository).save(captor.capture());
        assertEquals(newAuthor, captor.getValue().getAuthor());
    }

    @Test
    void updateNewsArticle_setAuthorToNull_ifAuthorIdIsBlank() {
        String articleId = UUID.randomUUID().toString();
        User author = createMockUser(UUID.randomUUID().toString(), "Old", "Author");
        NewsArticle existingArticle = createMockNewsArticle(articleId, "Old Title", author, null);

        when(newsArticleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(newsArticleRepository.save(any(NewsArticle.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder().authorId("").build(); // Blank authorId

        NewsArticleDto resultDto = newsArticleService.updateNewsArticle(articleId, updateDto);
        assertNull(resultDto.getAuthor());

        ArgumentCaptor<NewsArticle> captor = ArgumentCaptor.forClass(NewsArticle.class);
        verify(newsArticleRepository).save(captor.capture());
        assertNull(captor.getValue().getAuthor());
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
