package co.hublots.ln_foot.controllers;

// import co.hublots.ln_foot.dto.*; // Old DTOs not needed for new tests
import co.hublots.ln_foot.dto.SyncStatusDto;
// import co.hublots.ln_foot.services.SyncService; // Old service
import co.hublots.ln_foot.services.DataSyncService; // New service
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSyncService dataSyncService; // Changed from SyncService

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncAllFixtures_isOk_withAdminRole_andValidParams() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "2024-01-15");

        SyncStatusDto successDto = SyncStatusDto.builder()
                .status("SUCCESS")
                .message("Successfully synced fixtures.")
                .itemsProcessed(10)
                .build();
        when(dataSyncService.syncMainFixtures(anyMap())).thenReturn(successDto);

        mockMvc.perform(post("/api/v1/sync/all-fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.itemsProcessed", is(10)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncAllFixtures_isOk_withAdminRole_andEmptyBodyForParams() throws Exception {
        SyncStatusDto successDto = SyncStatusDto.builder()
                .status("SUCCESS")
                .message("Successfully synced fixtures with default params.")
                .itemsProcessed(5)
                .build();
        when(dataSyncService.syncMainFixtures(anyMap())).thenReturn(successDto); // Service receives empty map

        mockMvc.perform(post("/api/v1/sync/all-fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Empty JSON object
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.itemsProcessed", is(5)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void syncAllFixtures_whenServiceReturnsError_returnsErrorStatus() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("date", "2024-01-15");

        SyncStatusDto errorDto = SyncStatusDto.builder()
                .status("ERROR")
                .message("API Error: 500")
                .itemsProcessed(0)
                .build();
        when(dataSyncService.syncMainFixtures(anyMap())).thenReturn(errorDto);

        mockMvc.perform(post("/api/v1/sync/all-fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryParams)))
                .andExpect(status().isInternalServerError()) // Controller maps "ERROR" to 500
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", is("API Error: 500")));
    }


    @Test
    @WithAnonymousUser // Or simply no @WithMockUser
    void syncAllFixtures_isUnauthorized_withoutAuth() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        mockMvc.perform(post("/api/v1/sync/all-fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryParams)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin role
    void syncAllFixtures_isForbidden_withUserRole() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        mockMvc.perform(post("/api/v1/sync/all-fixtures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryParams)))
                .andExpect(status().isForbidden());
    }

    // Tests for old endpoints (e.g., /leagues, /teams-by-league) are removed
    // as the controller's endpoints have been consolidated.
}
