package co.hublots.ln_foot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
// import java.util.List; // For OneToMany relationships if user authors things

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    private String id;

    @Column(name = "keycloak_id", unique = true) // Assuming this maps to Keycloak's subject ID
    private String keycloakId;

    @Column(unique = true) // Often, app-level username is unique
    private String username;

    @Column(unique = true) // Email should generally be unique
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String role; // e.g., "ADMIN", "EDITOR", "USER"

    // Example for relationship if User can author NewsArticles
    // @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // private List<NewsArticle> newsArticles;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
