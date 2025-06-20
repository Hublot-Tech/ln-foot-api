package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.models.User.ValidRolesEnum;
import co.hublots.ln_foot.repositories.UserRepository;
import co.hublots.ln_foot.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private UserDto mapToDto(User entity) {
        if (entity == null) {
            return null;
        }
        String name = (entity.getFirstName() != null ? entity.getFirstName() : "") +
                (entity.getLastName() != null ? " " + entity.getLastName() : "");
        name = name.trim();

        return UserDto.builder()
                .id(entity.getId())
                .keycloakId(entity.getKeycloakId())
                .email(entity.getEmail())
                .name(name.isEmpty() ? null : name)
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .permissions(Collections.emptyList())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers(ValidRolesEnum role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(String id) {
        // Also consider findByKeycloakId if 'id' can be keycloakId
        return userRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(String userId, ValidRolesEnum newRole) { // Signature updated
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null or blank.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));


        user.setRole(newRole);

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + id + " not found"));

        // Check if the user is an admin
        if (ValidRolesEnum.ADMIN.equals(user.getRole())) {
            long adminCount = userRepository.countByRole(ValidRolesEnum.ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user.");
            }
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::mapToDto);
    }

    @Override
    @Transactional // Can be readOnly = true if findOrCreateUserFromJwt is also readOnly for existing users without updates
    public Optional<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return findOrCreateUserFromJwt(jwt);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<UserDto> findOrCreateUserFromJwt(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        String username = jwt.getClaimAsString("preferred_username");

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        @SuppressWarnings("unchecked")
        List<String> rolesFromJwt = realmAccess != null ? (List<String>) realmAccess.get("roles") : Collections.emptyList();

        ValidRolesEnum determinedRole = ValidRolesEnum.USER; // Default role
        if (rolesFromJwt != null) {
            if (rolesFromJwt.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role))) {
                determinedRole = ValidRolesEnum.ADMIN;
            } else if (rolesFromJwt.stream().anyMatch(role -> "USER".equalsIgnoreCase(role) || "ROLE_USER".equalsIgnoreCase(role))) {
                determinedRole = ValidRolesEnum.USER;
            }
        }

        Optional<User> existingUserOpt = userRepository.findByKeycloakId(keycloakId);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            boolean updated = false;
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }
            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }
            if (username != null && !username.equals(user.getUsername())) {
                user.setUsername(username);
                updated = true;
            }
            if (!determinedRole.equals(user.getRole())) {
                user.setRole(determinedRole);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
        } else {
            user = new User();
            user.setKeycloakId(keycloakId);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setRole(determinedRole);
            // Set other mandatory fields if any, e.g., createdAt, though usually handled by @CreationTimestamp
            user = userRepository.save(user);
        }
        return Optional.of(mapToDto(user));
    }
}
