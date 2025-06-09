package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.CreateHighlightDto;
import co.hublots.ln_foot.dto.HighlightDto;
import co.hublots.ln_foot.dto.UpdateHighlightDto;
import co.hublots.ln_foot.services.HighlightService;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class HighlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HighlightService highlightService;

    @Autowired
    private ObjectMapper objectMapper;

    private HighlightDto createMockHighlightDto(String id) {
        return HighlightDto.builder()
                .id(id)
                .fixtureId("fixture1")
                .title("Amazing Goal")
                .videoUrl("http://example.com/highlight.mp4")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser
    void listHighlights_isOk() throws Exception {
        HighlightDto mockHighlight = createMockHighlightDto("hl1");
        when(highlightService.listHighlights(any())).thenReturn(Collections.singletonList(mockHighlight));

        mockMvc.perform(get("/api/v1/highlights").param("fixtureId", "fix1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("hl1")));
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
        CreateHighlightDto createDto = CreateHighlightDto.builder().title("New Highlight").fixtureId("fix1").build();
        HighlightDto returnedDto = createMockHighlightDto(UUID.randomUUID().toString());
        returnedDto.setTitle("New Highlight");
        when(highlightService.createHighlight(any(CreateHighlightDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/highlights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Highlight")));
    }

    @Test
    void createHighlight_isUnauthorized_withoutAuth() throws Exception {
        CreateHighlightDto createDto = CreateHighlightDto.builder().build();
        mockMvc.perform(post("/api/v1/highlights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createHighlight_isForbidden_withUserRole() throws Exception {
        CreateHighlightDto createDto = CreateHighlightDto.builder().build();
        mockMvc.perform(post("/api/v1/highlights")
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

        mockMvc.perform(put("/api/v1/highlights/{id}", highlightId)
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

        mockMvc.perform(delete("/api/v1/highlights/{id}", highlightId))
                .andExpect(status().isNoContent());
        verify(highlightService, times(1)).deleteHighlight(highlightId);
    }
}
