package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.*; // Import all DTOs from the package
import co.hublots.ln_foot.services.NewsArticleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class NewsArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsArticleService newsArticleService;

    @Autowired
    private ObjectMapper objectMapper;

    private NewsArticleDto createMockNewsArticleDto(String id, String authorId, String authorName) {
        UserDto authorDto = null;
        if (authorId != null) {
            authorDto = UserDto.builder().id(authorId).name(authorName).role("EDITOR").build();
        }
        return NewsArticleDto.builder()
                .id(id)
                .title("Test Article")
                .content("Test content.")
                .author(authorDto) // Now a UserDto object
                .sourceName("Mock Source")
                .articleUrl("http://example.com/article/" + id)
                .publishedAt(OffsetDateTime.now())
                .tags(Collections.singletonList("test"))
                .status("published")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    // Overloaded method to fix calls with single argument (assumed to be ID)
    private NewsArticleDto createMockNewsArticleDto(String id) {
        // Provide default author details or null if appropriate for these test cases
        String defaultAuthorId = "default-author-" + UUID.randomUUID().toString().substring(0,4) ;
        String defaultAuthorName = "Default Author";
        return createMockNewsArticleDto(id, defaultAuthorId, defaultAuthorName);
    }

    @Test
    @WithAnonymousUser
    void listNewsArticles_isOk() throws Exception {
        NewsArticleDto mockArticle = createMockNewsArticleDto("na1"); // Now calls the new overloaded method
        when(newsArticleService.listNewsArticles(any(), any())).thenReturn(Collections.singletonList(mockArticle));

        mockMvc.perform(get("/api/v1/news-articles").param("status", "published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("na1")));
    }

    @Test
    @WithAnonymousUser
    void findNewsArticleById_isOk_whenFound() throws Exception {
        String articleId = "na123";
        NewsArticleDto mockArticle = createMockNewsArticleDto(articleId);
        when(newsArticleService.findNewsArticleById(articleId)).thenReturn(Optional.of(mockArticle));

        mockMvc.perform(get("/api/v1/news-articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(articleId)));
    }

    @Test
    @WithAnonymousUser
    void findNewsArticleById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(newsArticleService.findNewsArticleById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/news-articles/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- Admin/Editor Endpoint Tests ---
    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void createNewsArticle_isCreated_withAdminOrEditorRole() throws Exception { // Removed String role param, not used by @WithMockUser like this
        String authorId = UUID.randomUUID().toString();
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("New Article")
                .authorId(authorId) // Set authorId
                .content("Content")
                .build();

        NewsArticleDto returnedDto = createMockNewsArticleDto(UUID.randomUUID().toString(), authorId, "Mock Author");
        returnedDto.setTitle("New Article");
        // Ensure the author in returnedDto matches what would be mapped
        if (returnedDto.getAuthor() != null) { // Should not be null if authorId was provided
             assertEquals(authorId, returnedDto.getAuthor().getId());
        }


        when(newsArticleService.createNewsArticle(any(CreateNewsArticleDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/news-articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Article")));
    }

    @Test
    @WithMockUser(roles = "EDITOR") // Test specifically with EDITOR
    void createNewsArticle_isCreated_withEditorRole() throws Exception {
        String authorId = UUID.randomUUID().toString();
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                .title("Editor Article")
                .authorId(authorId)
                .content("Content by editor")
                .build();
        NewsArticleDto returnedDto = createMockNewsArticleDto(UUID.randomUUID().toString(), authorId, "Editor Name");
        returnedDto.setTitle("Editor Article");
        if (returnedDto.getAuthor() != null) {
             assertEquals(authorId, returnedDto.getAuthor().getId());
        }

        when(newsArticleService.createNewsArticle(any(CreateNewsArticleDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/news-articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Editor Article")));
    }

    @Test
    void createNewsArticle_isUnauthorized_withoutAuth() throws Exception {
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder().title("Attempt").authorId("someAuthor").build();
        mockMvc.perform(post("/api/v1/news-articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // A role that is not ADMIN or EDITOR
    void createNewsArticle_isForbidden_withUserRole() throws Exception {
        CreateNewsArticleDto createDto = CreateNewsArticleDto.builder().title("Forbidden Attempt").authorId("userAuthor").build();
        mockMvc.perform(post("/api/v1/news-articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void updateNewsArticle_isOk_withEditorRole() throws Exception {
        String articleId = "naToUpdate";
        String authorId = UUID.randomUUID().toString();
        UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder()
                .title("Updated Title")
                .authorId(authorId) // Assuming author can be updated
                .build();
        NewsArticleDto returnedDto = createMockNewsArticleDto(articleId, authorId, "Updated Author Name");
        returnedDto.setTitle("Updated Title");

        when(newsArticleService.updateNewsArticle(eq(articleId), any(UpdateNewsArticleDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/news-articles/{id}", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.author.id", is(authorId))); // Verify author update in response
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteNewsArticle_isNoContent_withAdminRole() throws Exception {
        String articleId = "naToDelete";
        doNothing().when(newsArticleService).deleteNewsArticle(articleId);

        mockMvc.perform(delete("/api/v1/news-articles/{id}", articleId))
                .andExpect(status().isNoContent());
        verify(newsArticleService, times(1)).deleteNewsArticle(articleId);
    }
}
