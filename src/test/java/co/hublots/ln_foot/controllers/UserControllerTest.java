package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto createMockUserDto(String id) {
        return UserDto.builder()
                .id(id)
                .email(id + "@example.com")
                .name("User " + id)
                .role("USER")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    // --- All UserController endpoints are Admin-Only ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsers_isOk_withAdminRole() throws Exception {
        UserDto mockUser = createMockUserDto("user1");
        when(userService.listUsers(any())).thenReturn(Collections.singletonList(mockUser));

        mockMvc.perform(get("/api/v1/users").param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("user1")));
    }

    @Test
    void listUsers_isUnauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void listUsers_isForbidden_withUserRole() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserById_isOk_withAdminRole_whenFound() throws Exception {
        String userId = "user123";
        UserDto mockUser = createMockUserDto(userId);
        when(userService.findUserById(userId)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserById_isNotFound_withAdminRole_whenServiceReturnsEmpty() throws Exception {
        when(userService.findUserById("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/users/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findUserById_isUnauthorized_withoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", "anyid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findUserById_isForbidden_withUserRole() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", "anyid"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_isOk_withAdminRole() throws Exception {
        String userId = "userToUpdate";
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("EDITOR").build();
        UserDto returnedDto = createMockUserDto(userId);
        returnedDto.setRole("EDITOR");

        when(userService.updateUserRole(eq(userId), any(UpdateUserRoleDto.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("EDITOR")));
    }

    @Test
    void updateUserRole_isUnauthorized_withoutAuth() throws Exception {
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("EDITOR").build();
        mockMvc.perform(put("/api/v1/users/{id}/role", "anyid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserRole_isForbidden_withUserRole() throws Exception {
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("EDITOR").build();
         mockMvc.perform(put("/api/v1/users/{id}/role", "anyid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_isNoContent_withAdminRole() throws Exception {
        String userId = "userToDelete";
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_isUnauthorized_withoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", "anyid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_isForbidden_withUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", "anyid"))
                .andExpect(status().isForbidden());
    }
}
