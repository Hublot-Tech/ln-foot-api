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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto createMockUserDto(String id, String role, String keycloakId) {
        return UserDto.builder()
                .id(id)
                .keycloakId(keycloakId)
                .email(id + "@example.com")
                .name("User " + id)
                .role(role)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private UserDto createMockUserDto(String id) {
        return createMockUserDto(id, "USER", "kc-" + id);
    }

    // --- /api/v1/users/me Tests ---
    @Test
    @WithMockUser // Default mock user: "user", roles "USER"
    void getCurrentUser_whenAuthenticatedAndUserFound_returnsUserDto() throws Exception {
        String userId = "currentUserId";
        String keycloakId = "current-user-keycloak-id"; // This would be `authentication.getName()` from
                                                        // @WithMockUser(username="...")
        UserDto expectedDto = createMockUserDto(userId, "USER", keycloakId);
        expectedDto.setName("CurrentUser");

        when(userService.getCurrentUser()).thenReturn(Optional.of(expectedDto));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId)))
                .andExpect(jsonPath("$.name", is("CurrentUser")))
                .andExpect(jsonPath("$.keycloakId", is(keycloakId)));

        verify(userService).getCurrentUser();
    }

    @Test
    @WithMockUser
    void getCurrentUser_whenAuthenticatedAndUserNotFound_returnsNotFound() throws Exception {
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isNotFound());

        verify(userService).getCurrentUser();
    }

    @Test
    void getCurrentUser_whenUnauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        verify(userService, times(0)).getCurrentUser(); // Service method should not be called
    }

    // --- Other User Endpoints ---
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

        when(userService.updateUserRole(eq(userId), eq(returnedDto.getRole()))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/users/{id}/role", userId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("EDITOR")));
    }

    @Test
    void updateUserRole_isUnauthorized_withoutAuth() throws Exception {
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("EDITOR").build();
        mockMvc.perform(put("/api/v1/users/{id}/role", "anyid").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUserRole_isForbidden_withUserRole() throws Exception {
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("EDITOR").build();
        mockMvc.perform(put("/api/v1/users/{id}/role", "anyid").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin-kc-id", roles = "ADMIN")
    void deleteUser_isNoContent_withAdminRole_whenDeletingAnotherUser() throws Exception {
        String userIdToDelete = "userToDelete";
        String userToDeleteKeycloakId = "other-user-kc-id"; // Different from admin-kc-id

        UserDto userToDeleteDto = createMockUserDto(userIdToDelete, "USER", userToDeleteKeycloakId);
        when(userService.findUserById(userIdToDelete)).thenReturn(Optional.of(userToDeleteDto));
        doNothing().when(userService).deleteUser(userIdToDelete);

        mockMvc.perform(delete("/api/v1/users/{id}", userIdToDelete).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).findUserById(userIdToDelete);
        verify(userService).deleteUser(userIdToDelete);
    }

    @Test
    @WithMockUser(username = "admin-kc-id", roles = "ADMIN")
    void deleteUser_byAdmin_whenDeletingSelf_isForbidden() throws Exception {
        String adminUserIdInDb = "adminUserInDb"; // This is the DB ID of the admin
        String adminKeycloakId = "admin-kc-id"; // This matches @WithMockUser's username

        UserDto adminDtoToDelete = createMockUserDto(adminUserIdInDb, "ADMIN", adminKeycloakId);
        when(userService.findUserById(adminUserIdInDb)).thenReturn(Optional.of(adminDtoToDelete));
        // deleteUser service method should not be called

        mockMvc.perform(delete("/api/v1/users/{id}", adminUserIdInDb).with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService).findUserById(adminUserIdInDb);
        verify(userService, times(0)).deleteUser(any(String.class));
    }

    @Test
    void deleteUser_isUnauthorized_withoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", "anyid").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_isForbidden_withUserRole() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", "anyid").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
