package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    // No specific PreAuthorize here, relies on global config for /api/v1/users/**
    // ensuring user is authenticated.
    public ResponseEntity<UserDto> getCurrentUser() {
        return userService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listUsers(
            @RequestParam(required = false) @Pattern(regexp = "^[A-Z_]{3,50}$", message = "Role must be uppercase letters and underscores, between 3 and 50 characters.") String role) {
        return userService.listUsers(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> findUserById(@PathVariable String id) { // Return type changed to specific DTO
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable String id,
            @Valid @RequestBody UpdateUserRoleDto updateUserRoleDto) { // Return type UserDto
        UserDto updatedUser = userService.updateUserRole(id, updateUserRoleDto.getRole());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, Authentication authentication) { // Return type Void
        String authenticatedUserKeycloakId = null;
        boolean isCurrentUserAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            authenticatedUserKeycloakId = authentication.getName(); // This is typically the Keycloak subject ID
            isCurrentUserAdmin = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }

        if (isCurrentUserAdmin) {
            Optional<UserDto> userToDeleteOpt = userService.findUserById(id); // findUserById uses internal app ID
            if (userToDeleteOpt.isPresent() && userToDeleteOpt.get().getKeycloakId() != null &&
                    userToDeleteOpt.get().getKeycloakId().equals(authenticatedUserKeycloakId)) {
                log.warn("Admin user (Keycloak ID: {}) attempted to delete their own account (User ID: {}).",
                        authenticatedUserKeycloakId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // No body for 403
            }
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
