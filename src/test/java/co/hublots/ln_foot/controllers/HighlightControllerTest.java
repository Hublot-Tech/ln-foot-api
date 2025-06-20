package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;

@SpringBootTest
@AutoConfigureMockMvc
class HighlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HighlightService highlightService;

    @Autowired
    private ObjectMapper objectMapper;

    private HighlightDto createMockHighlightDto(String id) {
        return HighlightDto.builder()
                .id(id)
                .title("Amazing Goal")
                .videoUrl("http://example.com/highlight.mp4")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser
    void listHighlightsByFixture_isOk_whenFixtureApiIdProvided() throws Exception {
        HighlightDto mockHighlight = createMockHighlightDto("hl1");
        Page<HighlightDto> highlightPage = new PageImpl<>(Collections.singletonList(mockHighlight),
                PageRequest.of(0, 5), 1);

        when(highlightService.listHighlights(any(Pageable.class))).thenReturn(highlightPage);

        mockMvc.perform(get("/api/v1/highlights")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("hl1")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithAnonymousUser
    void findHighlightById_isOk_whenFound() throws Exception {
        String highlightId = "hl123";
        HighlightDto mockHighlight = createMockHighlightDto(highlightId);
        when(highlightService.findHighlightById(highlightId)).thenReturn(Optional.of(mockHighlight));

        mockMvc.perform(get("/api/v1/highlights/{id}", highlightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(highlightId)));
    }

    @Test
    @WithAnonymousUser
    void findHighlightById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(highlightService.findHighlightById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/highlights/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    // --- Admin Endpoint Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void createHighlight_isCreated_withAdminRole() throws Exception {
        CreateHighlightDto createDto = CreateHighlightDto.builder().title("New Highlight").build();
        HighlightDto returnedDto = createMockHighlightDto(UUID.randomUUID().toString());
        returnedDto.setTitle("New Highlight");
        when(highlightService.createHighlight(any(CreateHighlightDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/highlights").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Highlight")));
    }

    @Test
    void createHighlight_isUnauthorized_withoutAuth() throws Exception {
        CreateHighlightDto createDto = CreateHighlightDto.builder().build();
        mockMvc.perform(post("/api/v1/highlights").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createHighlight_isForbidden_withUserRole() throws Exception {
        CreateHighlightDto createDto = CreateHighlightDto.builder().build();
        mockMvc.perform(post("/api/v1/highlights").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateHighlight_isOk_withAdminRole() throws Exception {
        String highlightId = "hlToUpdate";
        UpdateHighlightDto updateDto = UpdateHighlightDto.builder().title("Updated Title").build();
        HighlightDto returnedDto = createMockHighlightDto(highlightId);
        returnedDto.setTitle("Updated Title");

        when(highlightService.updateHighlight(eq(highlightId), any(UpdateHighlightDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/highlights/{id}", highlightId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteHighlight_isNoContent_withAdminRole() throws Exception {
        String highlightId = "hlToDelete";
        doNothing().when(highlightService).deleteHighlight(highlightId);

        mockMvc.perform(delete("/api/v1/highlights/{id}", highlightId).with(csrf()))
                .andExpect(status().isNoContent());
        verify(highlightService, times(1)).deleteHighlight(highlightId);
    }
}
