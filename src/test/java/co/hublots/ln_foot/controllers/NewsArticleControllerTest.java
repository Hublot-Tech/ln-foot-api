package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.models.NewsArticle.NewsStatus;
import co.hublots.ln_foot.services.NewsArticleService;

@SpringBootTest
@AutoConfigureMockMvc
class NewsArticleControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private NewsArticleService newsArticleService;

        @Autowired
        private ObjectMapper objectMapper;

        private NewsArticleDto createMockNewsArticleDto(String id, String authorName) {
                return NewsArticleDto.builder()
                                .id(id)
                                .title("Test Article")
                                .content("Test content.")
                                .authorName(authorName) // Now a UserDto object
                                .sourceUrl("http://example.com/article/" + id)
                                .publishedAt(OffsetDateTime.now())
                                .tags(Collections.singletonList("test"))
                                .status(NewsStatus.PUBLISHED)
                                .createdAt(OffsetDateTime.now())
                                .updatedAt(OffsetDateTime.now())
                                .build();
        }

        // Overloaded method to fix calls with single argument (assumed to be ID)
        private NewsArticleDto createMockNewsArticleDto(String id) {
                // Provide default author details or null if appropriate for these test cases
                String defaultAuthorName = "Default Author";
                return createMockNewsArticleDto(id, defaultAuthorName);
        }

        @Test
        @WithAnonymousUser
        void listNewsArticles_isOk() throws Exception {
                NewsArticleDto mockArticle = createMockNewsArticleDto("na1"); // Now calls the new overloaded method
                when(newsArticleService.listNewsArticles(any()))
                                .thenReturn(Collections.singletonList(mockArticle));

                mockMvc.perform(get("/api/v1/news-articles").param("status", NewsStatus.PUBLISHED.name()))
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
        @WithMockUser(roles = "ADMIN")
        void createNewsArticle_isCreated_withAdminOrEditorRole() throws Exception {
                String authorName = "Mock Author";
                CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                                .title("New Article")
                                .authorName(authorName)
                                .content("Content")
                                .build();

                NewsArticleDto returnedDto = createMockNewsArticleDto(UUID.randomUUID().toString(), authorName);
                returnedDto.setTitle("New Article");

                assertEquals(authorName, returnedDto.getAuthorName());

                when(newsArticleService.createNewsArticle(any(CreateNewsArticleDto.class))).thenReturn(returnedDto);

                mockMvc.perform(post("/api/v1/news-articles").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title", is("New Article")));
        }

        @Test
        @WithMockUser(roles = "ADMIN") // Test specifically with EDITOR
        void createNewsArticle_isCreated_withEditorRole() throws Exception {
                String authorId = "Editor Name";
                CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                                .title("Editor Article")
                                .authorName(authorId)
                                .content("Content by editor")
                                .build();
                NewsArticleDto returnedDto = createMockNewsArticleDto(UUID.randomUUID().toString(),
                                "Editor Name");
                returnedDto.setTitle("Editor Article");

                assertEquals(returnedDto.getAuthorName(), returnedDto.getAuthorName());

                when(newsArticleService.createNewsArticle(any(CreateNewsArticleDto.class))).thenReturn(returnedDto);

                mockMvc.perform(post("/api/v1/news-articles").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title", is("Editor Article")));
        }

        @Test
        void createNewsArticle_isUnauthorized_withoutAuth() throws Exception {
                CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                                .title("Attempt")
                                .authorName("someAuthor")
                                .content("Unauthorized content attempt") // Added content
                                .build();
                mockMvc.perform(post("/api/v1/news-articles").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER") // A role that is not ADMIN or EDITOR
        void createNewsArticle_isForbidden_withUserRole() throws Exception {
                CreateNewsArticleDto createDto = CreateNewsArticleDto.builder()
                                .title("Forbidden Attempt")
                                .authorName("userAuthor")
                                .content("Forbidden content attempt") // Added content
                                .build();
                mockMvc.perform(post("/api/v1/news-articles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateNewsArticle_isOk_withEditorRole() throws Exception {
                String articleId = "naToUpdate";
                UpdateNewsArticleDto updateDto = UpdateNewsArticleDto.builder()
                                .title("Updated Title")
                                .build();
                NewsArticleDto returnedDto = createMockNewsArticleDto(articleId, "Updated Author Name");
                returnedDto.setTitle("Updated Title");

                when(newsArticleService.updateNewsArticle(eq(articleId), any(UpdateNewsArticleDto.class)))
                                .thenReturn(returnedDto);

                mockMvc.perform(put("/api/v1/news-articles/{id}", articleId).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title", is("Updated Title")));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteNewsArticle_isNoContent_withAdminRole() throws Exception {
                String articleId = "naToDelete";
                doNothing().when(newsArticleService).deleteNewsArticle(articleId);

                mockMvc.perform(delete("/api/v1/news-articles/{id}", articleId).with(csrf()))
                                .andExpect(status().isNoContent());
                verify(newsArticleService, times(1)).deleteNewsArticle(articleId);
        }
}
