package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern; // For role pattern
import jakarta.validation.constraints.Size;   // For role size
import org.springframework.validation.annotation.Validated; // For class-level validation
import lombok.extern.slf4j.Slf4j; // For logging
import jakarta.persistence.EntityNotFoundException; // For try-catch
import org.springframework.dao.DataAccessException; // For general DB errors
import org.springframework.http.HttpStatus; // For status codes
import org.springframework.security.core.Authentication; // For self-deletion check
// For Keycloak specific principal:
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


import java.util.List;

@Slf4j // Added
@Validated // Added
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listUsers(
            @RequestParam(required = false)
            @Pattern(regexp = "^[A-Z_]{3,50}$", message = "Role must be uppercase letters and underscores, between 3 and 50 characters.")
            String role) {
        return userService.listUsers(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> findUserById(@PathVariable String id) { // Return type changed to specific DTO
        try {
            return userService.findUserById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("User not found with ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ID format for User: {}", id, e);
            return ResponseEntity.badRequest().build(); // No body for bad request with specific DTO type
        } catch (Exception e) {
            log.error("Error finding user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // No body for 500
        }
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable String id, @Valid @RequestBody UpdateUserRoleDto updateUserRoleDto) { // Return type UserDto
        try {
            UserDto updatedUser = userService.updateUserRole(id, updateUserRoleDto.getRole());
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            log.warn("User not found with ID {} when trying to update role: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role value provided for user ID {}: {}", id, updateUserRoleDto.getRole(), e);
            return ResponseEntity.badRequest().build(); // No body
        } catch (DataAccessException e) {
            log.error("Database error while updating role for user ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // No body
        }
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
            // Fetch the user to be deleted to compare its Keycloak ID
            Optional<UserDto> userToDeleteOpt = userService.findUserById(id); // findUserById uses internal app ID
            if (userToDeleteOpt.isPresent() && userToDeleteOpt.get().getKeycloakId() != null &&
                userToDeleteOpt.get().getKeycloakId().equals(authenticatedUserKeycloakId)) {
                log.warn("Admin user (Keycloak ID: {}) attempted to delete their own account (User ID: {}).", authenticatedUserKeycloakId, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // No body for 403
            }
            // If userToDeleteOpt is empty, it will be caught as EntityNotFoundException by deleteUser service call
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("User not found for deletion with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            log.error("Database error while deleting user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // No body
        }
    }
}
