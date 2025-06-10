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
    public ResponseEntity<?> findUserById(@PathVariable String id) { // Return type changed for error body
        try {
            return userService.findUserById(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.warn("User not found with ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid ID format for User: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error finding user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable String id, @Valid @RequestBody UpdateUserRoleDto updateUserRoleDto) {
        try {
            UserDto updatedUser = userService.updateUserRole(id, updateUserRoleDto);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            log.warn("User not found with ID {} when trying to update role: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role value provided for user ID {}: {}", id, updateUserRoleDto.getRole(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DataAccessException e) {
            log.error("Database error while updating role for user ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error during role update.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id, Authentication authentication) {
        String currentUserId = null;
        boolean isCurrentUserAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            // Assuming the principal's name is the Keycloak subject ID, which matches our User.keycloakId
            // or if User.id is directly the Keycloak subject ID.
            // This needs to align with how User entities are identified (by their own UUID or by Keycloak subject ID).
            // For this example, let's assume authentication.getName() is the ID used in the path.
            currentUserId = authentication.getName();
            isCurrentUserAdmin = authentication.getAuthorities().stream()
                                     .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        }

        // Fetch the user to be deleted to check if it's linked to the current admin via keycloakId
        // This check is a bit complex if User.id (path variable) is different from User.keycloakId (from Auth principal)
        // If User.id *is* the keycloakId, the check is simpler: id.equals(currentUserId)
        // For now, let's assume 'id' in path is the User entity's primary key (UUID).
        // We'd need to fetch the User entity for 'id' and check its 'keycloakId' against 'currentUserId'.
        // This is too complex without knowing the exact ID strategy.
        // Simplified check: if the 'id' in path is the same as the authenticated principal's name AND they are admin.
        // This assumes 'id' from path IS the same identifier as authentication.getName().
        if (id.equals(currentUserId) && isCurrentUserAdmin) {
             log.warn("Admin user {} (principal name) attempted to delete their own account (path ID {}).", currentUserId, id);
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admins cannot delete their own account using this endpoint.");
        }

        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("User not found for deletion with ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            log.error("Database error while deleting user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error during user deletion.");
        }
    }
}
