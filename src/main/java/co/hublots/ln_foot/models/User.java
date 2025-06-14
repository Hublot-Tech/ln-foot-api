package co.hublots.ln_foot.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "web_users")
public class User {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String role; // e.g., "ADMIN", "EDITOR", "USER"

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static enum ValidRolesEnum {
        ADMIN, EDITOR, USER;

        public static boolean isValidRole(String role) {
            for (ValidRolesEnum validRole : ValidRolesEnum.values()) {
                if (validRole.name().equalsIgnoreCase(role)) {
                    return true;
                }
            }
            return false;
        }
    }
}
