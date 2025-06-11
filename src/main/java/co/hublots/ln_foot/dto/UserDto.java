package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String keycloakId; // Added field
    private String email;
    private String name;
    private String avatarUrl;
    private String role; // e.g., "admin", "editor", "viewer"
    private List<String> permissions; // More granular permissions
    private OffsetDateTime emailVerified;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // Preferences or other user-specific settings can be added here
    // Example:
    // private UserPreferencesDto preferences;
}
