package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.repositories.UserRepository;
import co.hublots.ln_foot.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.core.context.SecurityContextHolder; // For getCurrentUser example
// import org.springframework.security.oauth2.jwt.Jwt; // For getCurrentUser example

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .email(entity.getEmail())
                .name(name.isEmpty() ? null : name)
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                // permissions and emailVerified are not in the current User entity
                .permissions(Collections.emptyList()) // Default to empty list
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    // No CreateUserDto, so no mapToEntityForCreate. Users are likely created via Keycloak sync or other process.
    // If there was a DTO to update general user info (e.g. UpdateUserDto), a mapToEntityForUpdate would be here.

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers(String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
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
    public UserDto updateUserRole(String id, UpdateUserRoleDto updateUserRoleDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + id + " not found"));

        if (updateUserRoleDto.getRole() != null) {
            user.setRole(updateUserRoleDto.getRole());
        }
        // 'permissions' from DTO are not mapped as User entity doesn't have a persistence field for it yet.
        // If it did, it would be: user.setPermissions(updateUserRoleDto.getPermissions());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with ID " + id + " not found");
        }
        // Consider business logic: e.g., cannot delete last admin, reassign content, etc.
        userRepository.deleteById(id);
    }
}
