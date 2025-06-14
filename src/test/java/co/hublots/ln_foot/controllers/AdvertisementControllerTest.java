package co.hublots.ln_foot.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.mockito.Mock;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.AdvertisementDto;
import co.hublots.ln_foot.dto.CreateAdvertisementDto;
import co.hublots.ln_foot.dto.UpdateAdvertisementDto;
import co.hublots.ln_foot.services.AdvertisementService;


@SpringBootTest
@AutoConfigureMockMvc
class AdvertisementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AdvertisementService advertisementService;

    @Autowired
    private ObjectMapper objectMapper;

    private AdvertisementDto createMockAdvertisementDto() {
        return AdvertisementDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Ad")
                .content("Test Content")
                .url("http://example.com")
                .imageUrl("http://example.com/image.png")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(7))
                .priority(1)
                .status("active")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @WithAnonymousUser // Explicitly state this runs as anonymous, expecting public access
    void getLatestAdvertisements_isOk_returnsPage() throws Exception {
        AdvertisementDto mockAd = createMockAdvertisementDto();
        Page<AdvertisementDto> mockPage = new PageImpl<>(Collections.singletonList(mockAd), PageRequest.of(0, 1), 1);

        when(advertisementService.getLatestAdvertisements(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/advertisements/latest")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Test Ad")))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithAnonymousUser
    void getAdvertisementById_isOk_whenFound() throws Exception {
        AdvertisementDto mockAd = createMockAdvertisementDto();
        when(advertisementService.getAdvertisementById(mockAd.getId())).thenReturn(Optional.of(mockAd));

        mockMvc.perform(get("/api/v1/advertisements/{id}", mockAd.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockAd.getId())))
                .andExpect(jsonPath("$.title", is(mockAd.getTitle())));
    }

    @Test
    @WithAnonymousUser
    void getAdvertisementById_isNotFound_whenServiceReturnsEmpty() throws Exception {
        when(advertisementService.getAdvertisementById("nonexistent-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/advertisements/{id}", "nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    // --- Create Advertisement Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void createAdvertisement_isCreated_withAdminRole() throws Exception {
        CreateAdvertisementDto createDto = CreateAdvertisementDto.builder().title("New Ad").build();
        AdvertisementDto returnedDto = createMockAdvertisementDto(); // Service returns full DTO
        returnedDto.setTitle("New Ad");

        when(advertisementService.createAdvertisement(any(CreateAdvertisementDto.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/advertisements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Ad")));
    }

    @Test
    void createAdvertisement_isUnauthorized_withoutAuth() throws Exception {
        CreateAdvertisementDto createDto = CreateAdvertisementDto.builder().title("New Ad").build();
        mockMvc.perform(post("/api/v1/advertisements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized()); // Default for unauthenticated by Spring Security
    }

    @Test
    @WithMockUser(roles = "USER") // Assuming "USER" is a non-admin role
    void createAdvertisement_isForbidden_withUserRole() throws Exception {
        CreateAdvertisementDto createDto = CreateAdvertisementDto.builder().title("New Ad").build();
        mockMvc.perform(post("/api/v1/advertisements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }

    // --- Update Advertisement Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAdvertisement_isOk_withAdminRole() throws Exception {
        String adId = "test-id";
        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder().title("Updated Ad").build();
        AdvertisementDto returnedDto = createMockAdvertisementDto();
        returnedDto.setId(adId);
        returnedDto.setTitle("Updated Ad");

        when(advertisementService.updateAdvertisement(eq(adId), any(UpdateAdvertisementDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/advertisements/{id}", adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Ad")));
    }

    @Test
    void updateAdvertisement_isUnauthorized_withoutAuth() throws Exception {
        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder().title("Updated Ad").build();
        mockMvc.perform(put("/api/v1/advertisements/{id}", "test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAdvertisement_isForbidden_withUserRole() throws Exception {
        UpdateAdvertisementDto updateDto = UpdateAdvertisementDto.builder().title("Updated Ad").build();
        mockMvc.perform(put("/api/v1/advertisements/{id}", "test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    // --- Delete Advertisement Tests ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAdvertisement_isNoContent_withAdminRole() throws Exception {
        String adId = "test-id-to-delete";
        doNothing().when(advertisementService).deleteAdvertisement(adId);

        mockMvc.perform(delete("/api/v1/advertisements/{id}", adId))
                .andExpect(status().isNoContent());
        verify(advertisementService, times(1)).deleteAdvertisement(adId);
    }

    @Test
    void deleteAdvertisement_isUnauthorized_withoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/advertisements/{id}", "test-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteAdvertisement_isForbidden_withUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/advertisements/{id}", "test-id"))
                .andExpect(status().isForbidden());
    }
}
