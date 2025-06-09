package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;
import org.springframework.stereotype.Service;
// import org.springframework.security.core.context.SecurityContextHolder; // For getCurrentUser
// import org.springframework.security.oauth2.jwt.Jwt; // For extracting info from JWT

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public List<UserDto> listUsers(String role) {
        // Mock: Filter by role if data was present
        return Collections.emptyList();
    }

    @Override
    public Optional<UserDto> findUserById(String id) {
        // Mock: Example of returning a specific mock User
        /*
        if ("test-user-id".equals(id)) {
            return Optional.of(UserDto.builder()
                    .id(id)
                    .email("testuser@example.com")
                    .name("Test User")
                    .avatarUrl("http://example.com/avatar.png")
                    .role("USER")
                    .emailVerified(OffsetDateTime.now().minusMonths(1))
                    .createdAt(OffsetDateTime.now().minusMonths(2))
                    .updatedAt(OffsetDateTime.now().minusDays(3))
                    .build());
        }
        */
        return Optional.empty();
    }

    @Override
    public UserDto updateUserRole(String id, UpdateUserRoleDto updateUserRoleDto) {
        // Assume user is fetched, then role updated
        return UserDto.builder()
                .id(id)
                .email("user" + id + "@example.com") // Mock email
                .name("User " + id) // Mock name
                .avatarUrl("http://example.com/avatar/" + id + ".png")
                .role(updateUserRoleDto.getRole()) // Updated role
                .permissions(updateUserRoleDto.getPermissions()) // Updated permissions
                .emailVerified(OffsetDateTime.now().minusMonths(1))
                .createdAt(OffsetDateTime.now().minusMonths(2)) // Original creation date
                .updatedAt(OffsetDateTime.now()) // New update date
                .build();
    }

    @Override
    public void deleteUser(String id) {
        // In a real app, this might involve more logic:
        // - Check if the user is trying to delete themselves (and disallow or handle)
        // - Anonymize or reassign user's content, etc.
        // - Ensure admin is not deleting the last admin account etc.
        System.out.println("Deleting user with id: " + id);
    }

    // Example for getCurrentUser if it were part of the interface:
    /*
    @Override
    public Optional<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Jwt)) {
            return Optional.empty();
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        // Assuming standard claims, adjust if your JWT structure is different
        String userId = jwt.getSubject(); // 'sub' claim often used for user ID
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name"); // or preferred_username
        String avatarUrl = jwt.getClaimAsString("picture"); // Common in OIDC
        // Role might come from 'roles' claim or a custom claim
        // List<String> roles = jwt.getClaimAsStringList("roles");
        // String role = roles != null && !roles.isEmpty() ? roles.get(0) : "USER";


        // This is a placeholder. You'd typically fetch the full UserDto from your database
        // using 'userId' or 'email' to get all details like createdAt, specific permissions etc.
        return Optional.of(UserDto.builder()
                .id(userId)
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                // .role(role) // Determine role based on JWT or DB lookup
                // .permissions(jwt.getClaimAsStringList("permissions"))
                .build());
    }
    */
}
